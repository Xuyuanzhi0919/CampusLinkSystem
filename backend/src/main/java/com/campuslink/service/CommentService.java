package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.comment.CommentResponse;
import com.campuslink.dto.comment.CreateCommentRequest;
import com.campuslink.entity.Comment;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务类
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    /**
     * 创建评论
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, CreateCommentRequest request) {
        // 验证用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 如果是回复,验证父评论是否存在
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getStatus() == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "父评论不存在或已删除");
            }
        }

        // 验证评论对象是否存在
        validateTargetExists(request.getTargetType(), request.getTargetId());

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setLikes(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());

        commentMapper.insert(comment);
        return comment.getCommentId();
    }

    /**
     * 删除评论（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论不存在或已删除");
        }

        // 只有评论作者可以删除
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "只能删除自己的评论");
        }

        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    /**
     * 获取评论列表（支持嵌套回复）
     */
    public PageResult<CommentResponse> getCommentList(
            String targetType,
            Long targetId,
            Integer page,
            Integer pageSize
    ) {
        Page<Comment> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, 1)
                .isNull(Comment::getParentId) // 只查询顶级评论
                .orderByDesc(Comment::getCreatedAt);

        IPage<Comment> commentPage = commentMapper.selectPage(pageObj, wrapper);

        List<CommentResponse> commentList = commentPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, true))
                .collect(Collectors.toList());

        return new PageResult<>(
                commentList,
                commentPage.getTotal(),
                commentPage.getCurrent(),
                commentPage.getSize(),
                commentPage.getPages()
        );
    }

    /**
     * 获取用户的评论列表
     */
    public PageResult<CommentResponse> getUserComments(
            Long userId,
            Integer page,
            Integer pageSize
    ) {
        Page<Comment> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreatedAt);

        IPage<Comment> commentPage = commentMapper.selectPage(pageObj, wrapper);

        List<CommentResponse> commentList = commentPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, false))
                .collect(Collectors.toList());

        return new PageResult<>(
                commentList,
                commentPage.getTotal(),
                commentPage.getCurrent(),
                commentPage.getSize(),
                commentPage.getPages()
        );
    }

    /**
     * 点赞评论
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer likeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论不存在或已删除");
        }

        comment.setLikes(comment.getLikes() + 1);
        commentMapper.updateById(comment);
        return comment.getLikes();
    }

    /**
     * 取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论不存在或已删除");
        }

        if (comment.getLikes() > 0) {
            comment.setLikes(comment.getLikes() - 1);
            commentMapper.updateById(comment);
        }
        return comment.getLikes();
    }

    /**
     * 获取评论统计
     */
    public Long getCommentCount(String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, 1);
        return commentMapper.selectCount(wrapper);
    }

    /**
     * 转换为响应DTO
     */
    private CommentResponse convertToResponse(Comment comment, boolean includeReplies) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setUserId(comment.getUserId());
        response.setTargetType(comment.getTargetType());
        response.setTargetId(comment.getTargetId());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParentId());
        response.setLikes(comment.getLikes());
        response.setCreatedAt(comment.getCreatedAt());
        // 当前版本暂不支持点赞状态查询,默认返回false
        response.setIsLiked(false);

        // 查询评论者信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname());
            response.setUserAvatar(user.getAvatarUrl());
        }

        // 如果有父评论,查询父评论用户名
        if (comment.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                User parentUser = userMapper.selectById(parentComment.getUserId());
                if (parentUser != null) {
                    response.setParentUserName(parentUser.getNickname());
                }
            }
        }

        // 如果需要包含回复,查询该评论的所有回复
        if (includeReplies) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, comment.getCommentId())
                    .eq(Comment::getStatus, 1)
                    .orderByAsc(Comment::getCreatedAt);

            List<Comment> replies = commentMapper.selectList(replyWrapper);
            List<CommentResponse> replyList = replies.stream()
                    .map(reply -> convertToResponse(reply, false))
                    .collect(Collectors.toList());

            response.setReplies(replyList);
            response.setReplyCount(replyList.size());
        } else {
            response.setReplies(new ArrayList<>());
            response.setReplyCount(0);
        }

        return response;
    }

    /**
     * 验证评论对象是否存在
     */
    private void validateTargetExists(String targetType, Long targetId) {
        switch (targetType) {
            case "resource":
                if (resourceMapper.selectById(targetId) == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
                }
                break;
            case "question":
                if (questionMapper.selectById(targetId) == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "问题不存在");
                }
                break;
            case "answer":
                if (answerMapper.selectById(targetId) == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "回答不存在");
                }
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的评论对象类型");
        }
    }
}
