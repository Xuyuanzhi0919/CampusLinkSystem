package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.question.*;
import com.campuslink.entity.Answer;
import com.campuslink.entity.AnswerLike;
import com.campuslink.entity.Question;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.AnswerLikeMapper;
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
    private final AnswerLikeMapper answerLikeMapper;

    /**
     * 提问
     */
    @Transactional(rollbackFor = Exception.class)
    public Long askQuestion(Long userId, AskQuestionRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 计算需要扣除的总积分：提问2分 + 悬赏积分
        int bounty = request.getBounty() != null ? request.getBounty() : 0;
        int totalCost = 2 + bounty; // 提问固定扣2分

        // 检查用户积分是否足够
        if (user.getPoints() < totalCost) {
            throw new BusinessException(ResultCode.INSUFFICIENT_POINTS);
        }

        // 扣除积分
        user.setPoints(user.getPoints() - totalCost);
        userMapper.updateById(user);

        // 创建问题
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setAskerId(userId);
        question.setCategory(request.getCategory());
        question.setTags(convertListToJson(request.getTags()));
        question.setImages(convertListToJson(request.getImages()));
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
     * 更新问题
     */
    public void updateQuestion(Long userId, Long questionId, AskQuestionRequest request) {
        // 查询问题
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 检查权限：只有提问者本人可以编辑
        if (!question.getAskerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 检查问题状态：已解决的问题不能编辑
        if (question.getIsSolved() == 1) {
            throw new BusinessException(ResultCode.QUESTION_ALREADY_SOLVED, "已解决的问题不能编辑");
        }

        // 更新问题信息
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setCategory(request.getCategory());
        question.setTags(convertListToJson(request.getTags()));
        question.setImages(convertListToJson(request.getImages()));
        question.setUpdatedAt(LocalDateTime.now());

        // 注意：悬赏积分在编辑时不允许修改，因为可能已经有人回答了
        // 如果需要支持修改悬赏，需要额外的业务逻辑处理

        questionMapper.updateById(question);
    }

    /**
     * 获取问题列表
     */
    public PageResult<QuestionListResponse> getQuestionList(
            String category, Long schoolId, String keyword, Integer isSolved,
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
        if (isSolved != null) {
            wrapper.eq(Question::getIsSolved, isSolved);
        }

        // 排序
        if ("created_at".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getCreatedAt);
        } else if ("views".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getViews);
        } else if ("bounty".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getRewardPoints);
        } else if ("answerCount".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Question::getAnswerCount);
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

            // 获取提问者信息（昵称、头像和等级）
            User asker = userMapper.selectById(question.getAskerId());
            if (asker != null) {
                response.setAskerNickname(asker.getNickname());
                response.setAskerAvatar(asker.getAvatarUrl());
                response.setAskerLevel(asker.getLevel() != null ? asker.getLevel() : 1); // 默认等级1
            } else {
                // 用户不存在时的容错处理
                response.setAskerNickname("用户已注销");
                response.setAskerAvatar(null);
                response.setAskerLevel(0);
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

        // 解析JSON字段
        response.setTags(parseJsonArray(question.getTags()));
        response.setImages(parseJsonArray(question.getImages()));
        response.setAiAnswer(question.getAiAnswer());
        response.setAiGeneratedAt(question.getAiGeneratedAt());

        // 获取提问者信息
        User asker = userMapper.selectById(question.getAskerId());
        if (asker != null) {
            response.setAskerNickname(asker.getNickname());
            response.setAskerAvatar(asker.getAvatarUrl());
            response.setAskerLevel(asker.getLevel() != null ? asker.getLevel() : 1); // 默认等级1
        } else {
            // 用户不存在时的容错处理
            response.setAskerNickname("用户已注销");
            response.setAskerAvatar(null);
            response.setAskerLevel(0);
        }

        return response;
    }

    /**
     * 将List转换为JSON数组字符串
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 解析JSON数组字符串
     */
    private List<String> parseJsonArray(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 简单的JSON数组解析(去掉方括号和引号)
            String content = jsonStr.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
            }
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            // 分割并清理每个元素
            String[] items = content.split(",");
            List<String> result = new ArrayList<>();
            for (String item : items) {
                String cleaned = item.trim().replaceAll("^\"|\"$", "");
                if (!cleaned.isEmpty()) {
                    result.add(cleaned);
                }
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

        // 防止提问者回答自己的问题
        if (question.getAskerId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能回答自己的问题");
        }

        // 检查用户是否已经回答过这个问题（防止刷积分）
        LambdaQueryWrapper<Answer> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Answer::getQuestionId, questionId)
                .eq(Answer::getResponderId, userId)
                .eq(Answer::getStatus, 1);
        Long existingAnswerCount = answerMapper.selectCount(checkWrapper);
        if (existingAnswerCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您已经回答过这个问题了");
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

        // 回答者获得 5 积分
        user.setPoints(user.getPoints() + 5);
        userMapper.updateById(user);

        // 更新问题的回答数量
        question.setAnswerCount(question.getAnswerCount() + 1);
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);

        return answer.getAid();
    }

    /**
     * 获取问题的所有答案
     */
    public List<AnswerResponse> getAnswersByQuestionId(Long questionId, Long currentUserId) {
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
            response.setAid(answer.getAid());
            response.setQuestionId(answer.getQuestionId());
            response.setAnswererId(answer.getResponderId());
            response.setContent(answer.getContent());
            response.setLikes(answer.getLikes());
            response.setIsAccepted(answer.getIsAccepted() == 1);  // 转换为Boolean

            // 查询当前用户是否已点赞
            boolean isLiked = false;
            if (currentUserId != null) {
                LambdaQueryWrapper<AnswerLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(AnswerLike::getUserId, currentUserId)
                        .eq(AnswerLike::getAnswerId, answer.getAid());
                Long likeCount = answerLikeMapper.selectCount(likeWrapper);
                isLiked = likeCount > 0;
            }
            response.setIsLiked(isLiked);

            response.setCreatedAt(answer.getCreatedAt());
            response.setUpdatedAt(answer.getUpdatedAt());

            // 解析图片JSON (如果Answer实体有images字段)
            // response.setImages(parseJsonArray(answer.getImages()));

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
     * 获取我的提问列表
     */
    public PageResult<QuestionListResponse> getMyQuestions(Long userId, Integer page, Integer pageSize, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getAskerId, userId)
                .eq(Question::getStatus, 1); // 只查询正常状态的问题

        // 如果指定了status参数,按状态筛选
        if (status != null) {
            wrapper.eq(Question::getIsSolved, status);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Question::getCreatedAt);

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
            response.setStatus(question.getIsSolved());
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
    public Integer likeAnswer(Long userId, Long answerId) {
        // 验证答案是否存在
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        // 不能给自己的回答点赞
        if (answer.getResponderId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能给自己的回答点赞");
        }

        // 检查是否已经点赞
        LambdaQueryWrapper<AnswerLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerLike::getUserId, userId)
                .eq(AnswerLike::getAnswerId, answerId);
        Long existingLikeCount = answerLikeMapper.selectCount(wrapper);

        if (existingLikeCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您已经点赞过该答案");
        }

        // 创建点赞记录
        AnswerLike answerLike = new AnswerLike();
        answerLike.setUserId(userId);
        answerLike.setAnswerId(answerId);
        answerLike.setCreatedAt(LocalDateTime.now());
        answerLikeMapper.insert(answerLike);

        // 更新点赞数
        answer.setLikes(answer.getLikes() + 1);
        answerMapper.updateById(answer);

        return answer.getLikes();
    }

    /**
     * 取消点赞答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeAnswer(Long userId, Long answerId) {
        // 验证答案是否存在
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        // 查找点赞记录
        LambdaQueryWrapper<AnswerLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerLike::getUserId, userId)
                .eq(AnswerLike::getAnswerId, answerId);
        AnswerLike answerLike = answerLikeMapper.selectOne(wrapper);

        if (answerLike == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您还未点赞该答案");
        }

        // 删除点赞记录
        answerLikeMapper.deleteById(answerLike.getLikeId());

        // 更新点赞数
        if (answer.getLikes() > 0) {
            answer.setLikes(answer.getLikes() - 1);
            answerMapper.updateById(answer);
        }

        return answer.getLikes();
    }

    /**
     * 删除答案
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnswer(Long userId, Long answerId) {
        // 检查答案是否存在
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() == 0) {
            throw new BusinessException(ResultCode.ANSWER_NOT_FOUND);
        }

        // 检查是否是答案作者本人
        if (!answer.getResponderId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 检查答案是否已被采纳
        if (answer.getIsAccepted() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已被采纳的答案不能删除");
        }

        // 扣除回答获得的积分（回答问题的5分）
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 回答问题获得的积分
            int deductPoints = 5;
            // 确保积分不会变成负数
            user.setPoints(Math.max(0, user.getPoints() - deductPoints));
            userMapper.updateById(user);
        }

        // 软删除答案
        answer.setStatus(0);
        answer.setUpdatedAt(LocalDateTime.now());
        answerMapper.updateById(answer);

        // 更新问题的回答数量
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question != null && question.getAnswerCount() > 0) {
            question.setAnswerCount(question.getAnswerCount() - 1);
            question.setUpdatedAt(LocalDateTime.now());
            questionMapper.updateById(question);
        }
    }

    /**
     * 删除问题
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long userId, Long questionId) {
        // 检查问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 检查是否是提问者本人
        if (!question.getAskerId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 检查问题是否已解决(已解决的问题不能删除)
        if (question.getIsSolved() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已解决的问题不能删除");
        }

        // 退还积分：提问的2分 + 悬赏积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            int refundPoints = 2 + (question.getRewardPoints() != null ? question.getRewardPoints() : 0);
            user.setPoints(user.getPoints() + refundPoints);
            userMapper.updateById(user);
        }

        // 软删除问题
        question.setStatus(0);
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);

        // 软删除该问题下的所有答案
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getQuestionId, questionId)
                .eq(Answer::getStatus, 1);
        List<Answer> answers = answerMapper.selectList(wrapper);
        for (Answer answer : answers) {
            answer.setStatus(0);
            answer.setUpdatedAt(LocalDateTime.now());
            answerMapper.updateById(answer);
        }
    }

    /**
     * 获取热门标签
     */
    public List<HotTagResponse> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 8;
        }

        // 查询最近30天的问题（正常状态）
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1)
                .ge(Question::getCreatedAt, LocalDateTime.now().minusDays(30))
                .isNotNull(Question::getTags);

        List<Question> questions = questionMapper.selectList(wrapper);

        // 统计标签出现次数
        java.util.Map<String, Integer> tagCountMap = new java.util.HashMap<>();
        for (Question question : questions) {
            String tagsJson = question.getTags();
            if (tagsJson != null && !tagsJson.isEmpty()) {
                // 解析JSON数组格式的标签字符串: ["标签1","标签2"]
                String[] tags = tagsJson
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .split(",");

                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCountMap.put(trimmedTag, tagCountMap.getOrDefault(trimmedTag, 0) + 1);
                    }
                }
            }
        }

        // 按出现次数排序并取前N个
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> HotTagResponse.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取活跃答主
     */
    public List<ActiveUserResponse> getActiveUsers(Integer limit, String period) {
        if (limit == null || limit <= 0) {
            limit = 4;
        }

        // 计算时间范围（默认最近7天）
        LocalDateTime startTime;
        if ("30d".equals(period)) {
            startTime = LocalDateTime.now().minusDays(30);
        } else {
            startTime = LocalDateTime.now().minusDays(7);
        }

        // 查询最近活跃的答案（正常状态）
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getStatus, 1)
                .ge(Answer::getCreatedAt, startTime);

        List<Answer> answers = answerMapper.selectList(wrapper);

        // 统计每个用户的回答数量
        java.util.Map<Long, Integer> userAnswerCountMap = new java.util.HashMap<>();
        for (Answer answer : answers) {
            Long userId = answer.getResponderId();
            userAnswerCountMap.put(userId, userAnswerCountMap.getOrDefault(userId, 0) + 1);
        }

        // 按回答数排序并取前N个用户
        List<Long> topUserIds = userAnswerCountMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());

        // 查询用户信息并构建响应
        List<ActiveUserResponse> responses = new ArrayList<>();
        for (Long userId : topUserIds) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                Integer answerCount = userAnswerCountMap.get(userId);

                // 根据回答数量设置徽章
                String badge = null;
                if (answerCount >= 50) {
                    badge = "优质答主";
                } else if (answerCount >= 20) {
                    badge = "热心答主";
                } else if (answerCount >= 10) {
                    badge = "活跃答主";
                }

                responses.add(ActiveUserResponse.builder()
                        .userId(user.getUId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatarUrl())
                        .answerCount(answerCount)
                        .badge(badge)
                        .build());
            }
        }

        return responses;
    }

    /**
     * 获取精选问题列表（用于首页推荐位轮播）
     * 筛选规则：
     * 1. 至少3个回答
     * 2. 发布时间在7天内
     * 3. 未被删除、正常状态
     * 4. 按综合质量分数排序
     *
     * 质量分数 = 回答数 * 3 + 浏览数 * 0.002 + 悬赏积分 * 0.5 + 时间衰减
     *
     * @param limit 返回数量，默认5条
     * @return 精选问题列表
     */
    public List<FeaturedQuestionResponse> getFeaturedQuestions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5;  // 默认返回5条
        }

        // 查询7天内的问题
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1)  // status=1 表示正常
                .eq(Question::getIsSolved, 0)  // 优先未解决的问题
                .ge(Question::getCreatedAt, sevenDaysAgo)
                .orderByDesc(Question::getCreatedAt);

        List<Question> questions = questionMapper.selectList(wrapper);

        // 计算每个问题的质量分数
        List<FeaturedQuestionResponse> featuredList = new ArrayList<>();

        for (Question question : questions) {
            // 获取回答数
            LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(Answer::getQuestionId, question.getQid())
                    .eq(Answer::getStatus, 1);
            long answerCount = answerMapper.selectCount(answerWrapper);

            // 至少3个回答才能入选
            if (answerCount < 3) {
                continue;
            }

            // 计算天数衰减（天数越少，分数越高）
            long daysOld = java.time.Duration.between(question.getCreatedAt(), LocalDateTime.now()).toDays();
            double timeDecay = Math.max(0, 7 - daysOld);  // 7天内线性衰减

            // 计算质量分数
            double score = answerCount * 3.0  // 回答数权重最高
                    + question.getViews() * 0.002  // 浏览数权重
                    + (question.getRewardPoints() != null ? question.getRewardPoints() * 0.5 : 0)  // 悬赏积分
                    + timeDecay;  // 时间衰减

            // 查询提问者信息
            User user = userMapper.selectById(question.getAskerId());
            if (user != null) {
                FeaturedQuestionResponse response = FeaturedQuestionResponse.builder()
                        .qid(question.getQid())
                        .title(question.getTitle())
                        .username(user.getNickname())
                        .avatar(user.getAvatarUrl())
                        .category(question.getCategory())
                        .answerCount((int) answerCount)
                        .views(question.getViews())
                        .likes(0)  // Question表没有likes字段，默认为0
                        .createdAt(question.getCreatedAt())
                        .qualityScore(score)
                        .build();
                featuredList.add(response);
            }
        }

        // 按质量分数降序排序，取前N条
        return featuredList.stream()
                .sorted((q1, q2) -> Double.compare(q2.getQualityScore(), q1.getQualityScore()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
}
