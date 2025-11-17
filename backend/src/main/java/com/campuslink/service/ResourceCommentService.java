package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.comment.AddCommentRequest;
import com.campuslink.dto.comment.ResourceCommentResponse;
import com.campuslink.entity.ResourceComment;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ResourceCommentMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceCommentService {

    private final ResourceCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long resourceId, Long userId, AddCommentRequest request) {
        if (request.getParentId() != null) {
            ResourceComment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Parent comment not found");
            }
            if (!parentComment.getResourceId().equals(resourceId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid parent comment");
            }
            if (parentComment.getParentId() != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "3-level comments not supported");
            }
        }

        ResourceComment comment = new ResourceComment();
        comment.setResourceId(resourceId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setDeleted(0);

        commentMapper.insert(comment);
        return comment.getCommentId();
    }

    public PageResult<ResourceCommentResponse> getResourceComments(Long resourceId, Integer page, Integer pageSize) {
        Page<ResourceComment> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<ResourceComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceComment::getResourceId, resourceId)
                .isNull(ResourceComment::getParentId)
                .orderByDesc(ResourceComment::getCreatedAt);

        IPage<ResourceComment> commentPage = commentMapper.selectPage(pageObj, queryWrapper);

        List<ResourceCommentResponse> commentList = commentPage.getRecords().stream()
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        ResourceComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Comment not found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "Permission denied");
        }

        commentMapper.deleteById(commentId);

        if (comment.getParentId() == null) {
            LambdaQueryWrapper<ResourceComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceComment::getParentId, commentId);
            commentMapper.delete(wrapper);
        }
    }

    /**
     * 统计资源的评论数量（包括一级评论和回复）
     */
    public Long countByResourceId(Long resourceId) {
        LambdaQueryWrapper<ResourceComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceComment::getResourceId, resourceId);
        return commentMapper.selectCount(wrapper);
    }

    private ResourceCommentResponse convertToResponse(ResourceComment comment, boolean loadReplies) {
        ResourceCommentResponse response = new ResourceCommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setResourceId(comment.getResourceId());
        response.setUserId(comment.getUserId());
        response.setParentId(comment.getParentId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname());
            response.setUserAvatar(user.getAvatarUrl());
        }

        if (loadReplies && comment.getParentId() == null) {
            LambdaQueryWrapper<ResourceComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceComment::getParentId, comment.getCommentId())
                    .orderByAsc(ResourceComment::getCreatedAt);

            List<ResourceComment> replyList = commentMapper.selectList(wrapper);
            response.setReplies(
                    replyList.stream()
                            .map(reply -> convertToResponse(reply, false))
                            .collect(Collectors.toList())
            );
            response.setReplyCount(replyList.size());
        } else {
            response.setReplies(new ArrayList<>());
            response.setReplyCount(0);
        }

        return response;
    }
}
