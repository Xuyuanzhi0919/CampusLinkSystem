package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.question.*;
import com.campuslink.entity.Answer;
import com.campuslink.entity.Question;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.AnswerMapper;
import com.campuslink.mapper.QuestionMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 问答服务
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;

    /**
     * 提问
     */
    @Transactional(rollbackFor = Exception.class)
    public Long askQuestion(Long userId, AskQuestionRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 如果设置了悬赏,检查用户积分是否足够
        if (request.getBounty() != null && request.getBounty() > 0) {
            if (user.getPoints() < request.getBounty()) {
                throw new BusinessException(ResultCode.INSUFFICIENT_POINTS);
            }
            // 扣除悬赏积分
            user.setPoints(user.getPoints() - request.getBounty());
            userMapper.updateById(user);
        }

        // 创建问题
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setAskerId(userId);
        question.setCategory(request.getCategory());
        question.setViews(0);
        question.setAnswerCount(0);
        question.setIsSolved(0); // 未解决
        question.setRewardPoints(request.getBounty() != null ? request.getBounty() : 0);
        question.setStatus(1); // 正常
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        questionMapper.insert(question);
        return question.getQid();
    }

    /**
     * 获取问题列表
     */
    public PageResult<QuestionListResponse> getQuestionList(
            String category, Long schoolId, String keyword,
            Integer page, Integer pageSize, String sortBy, String sortOrder
    ) {
        // 构建查询条件
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1); // 只查询正常状态的问题

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Question::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Question::getTitle, keyword)
                    .or()
                    .like(Question::getContent, keyword));
        }

        // 排序
        if ("created_at".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getCreatedAt);
        } else if ("views".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getViews);
        } else if ("bounty".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getRewardPoints);
        }

        // 分页查询
        Page<Question> pageObj = new Page<>(page, pageSize);
        Page<Question> result = questionMapper.selectPage(pageObj, wrapper);

        // 转换为响应对象
        List<QuestionListResponse> responses = new ArrayList<>();
        for (Question question : result.getRecords()) {
            QuestionListResponse response = new QuestionListResponse();
            response.setQid(question.getQid());
            response.setTitle(question.getTitle());
            response.setCategory(question.getCategory());
            response.setBounty(question.getRewardPoints());
            response.setViews(question.getViews());
            response.setAnswerCount(question.getAnswerCount());
            response.setStatus(question.getIsSolved()); // 使用isSolved作为status
            response.setCreatedAt(question.getCreatedAt());

            // 获取提问者昵称
            User asker = userMapper.selectById(question.getAskerId());
            if (asker != null) {
                response.setAskerNickname(asker.getNickname());
            }

            responses.add(response);
        }

        // 构建分页结果
        PageResult<QuestionListResponse> pageResult = new PageResult<>();
        pageResult.setList(responses);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) page);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotalPages((result.getTotal() + pageSize - 1) / pageSize);

        return pageResult;
    }

    /**
     * 获取问题详情
     */
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 增加浏览次数
        question.setViews(question.getViews() + 1);
        questionMapper.updateById(question);

        // 转换为响应对象
        QuestionResponse response = new QuestionResponse();
        response.setQid(question.getQid());
        response.setTitle(question.getTitle());
        response.setContent(question.getContent());
        response.setAskerId(question.getAskerId());
        response.setCategory(question.getCategory());
        response.setBounty(question.getRewardPoints());
        response.setViews(question.getViews());
        response.setAnswerCount(question.getAnswerCount());
        response.setStatus(question.getIsSolved());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());

        // 获取提问者信息
        User asker = userMapper.selectById(question.getAskerId());
        if (asker != null) {
            response.setAskerNickname(asker.getNickname());
            response.setAskerAvatar(asker.getAvatarUrl());
        }

        return response;
    }

    /**
     * 回答问题
     */
    @Transactional(rollbackFor = Exception.class)
    public Long answerQuestion(Long userId, Long questionId, AnswerQuestionRequest request) {
        // 检查问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 检查问题是否已解决
        if (question.getIsSolved() == 1) {
            throw new BusinessException(ResultCode.QUESTION_ALREADY_SOLVED);
        }

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 创建答案
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setResponderId(userId);
        answer.setContent(request.getContent());
        answer.setLikes(0);
        answer.setIsAccepted(0);
        answer.setStatus(1); // 正常
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());

        answerMapper.insert(answer);

        // 更新问题的回答数量
        question.setAnswerCount(question.getAnswerCount() + 1);
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);

        return answer.getAid();
    }

    /**
     * 获取问题的所有答案
     */
    public List<AnswerResponse> getAnswersByQuestionId(Long questionId) {
        // 检查问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 查询答案列表
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getQuestionId, questionId)
                .eq(Answer::getStatus, 1) // 只查询正常状态的答案
                .orderByDesc(Answer::getIsAccepted) // 已采纳的答案排在前面
                .orderByDesc(Answer::getLikes)      // 按点赞数排序
                .orderByDesc(Answer::getCreatedAt); // 按创建时间排序

        List<Answer> answers = answerMapper.selectList(wrapper);

        // 转换为响应对象
        List<AnswerResponse> responses = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerResponse response = new AnswerResponse();
            BeanUtils.copyProperties(answer, response);
            response.setAnswererId(answer.getResponderId());

            // 获取回答者信息
            User answerer = userMapper.selectById(answer.getResponderId());
            if (answerer != null) {
                response.setAnswererNickname(answerer.getNickname());
                response.setAnswererAvatar(answerer.getAvatarUrl());
            }

            responses.add(response);
        }

        return responses;
    }

    /**
     * 采纳答案
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptAnswer(Long userId, Long questionId, Long answerId) {
        // 检查问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 检查是否是提问者本人
        if (!question.getAskerId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 检查问题是否已解决
        if (question.getIsSolved() == 1) {
            throw new BusinessException(ResultCode.QUESTION_ALREADY_SOLVED);
        }

        // 检查答案是否存在
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || !answer.getQuestionId().equals(questionId) || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        // 标记答案为已采纳
        answer.setIsAccepted(1);
        answer.setUpdatedAt(LocalDateTime.now());
        answerMapper.updateById(answer);

        // 更新问题状态为已解决
        question.setIsSolved(1);
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);

        // 奖励回答者
        User answerer = userMapper.selectById(answer.getResponderId());
        if (answerer != null && question.getRewardPoints() > 0) {
            answerer.setPoints(answerer.getPoints() + question.getRewardPoints());
            userMapper.updateById(answerer);
        }
    }

    /**
     * 点赞问题（暂不实现,数据库没有likes字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer likeQuestion(Long questionId) {
        throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "问题点赞功能暂未开放");
    }

    /**
     * 取消点赞问题（暂不实现,数据库没有likes字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeQuestion(Long questionId) {
        throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "问题点赞功能暂未开放");
    }

    /**
     * 点赞答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer likeAnswer(Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        answer.setLikes(answer.getLikes() + 1);
        answerMapper.updateById(answer);
        return answer.getLikes();
    }

    /**
     * 取消点赞答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeAnswer(Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        if (answer.getLikes() > 0) {
            answer.setLikes(answer.getLikes() - 1);
            answerMapper.updateById(answer);
        }
        return answer.getLikes();
    }
}
