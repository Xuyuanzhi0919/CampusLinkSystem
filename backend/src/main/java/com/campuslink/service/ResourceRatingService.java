package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.ResourceRating;
import com.campuslink.mapper.ResourceRatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源评分服务
 */
@Service
@RequiredArgsConstructor
public class ResourceRatingService {
    private final ResourceRatingMapper resourceRatingMapper;

    /**
     * 用户对资源进行评分
     *
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param rating     评分（1-5）
     * @return 资源的新评分信息（平均分、总评分人数、用户评分）
     */
    @Transactional
    public RatingResult rateResource(Long userId, Long resourceId, Integer rating) {
        // 验证评分范围
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("评分必须在0-5之间");
        }

        // 查询用户是否已评分
        LambdaQueryWrapper<ResourceRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRating::getUserId, userId)
                .eq(ResourceRating::getResourceId, resourceId);

        ResourceRating existingRating = resourceRatingMapper.selectOne(wrapper);

        if (rating == 0) {
            // rating=0表示取消评分
            if (existingRating != null) {
                resourceRatingMapper.deleteById(existingRating.getRatingId());
            }
        } else {
            // rating>0表示评分或更新评分
            if (existingRating != null) {
                // 更新现有评分
                existingRating.setRating(rating);
                existingRating.setUpdatedAt(LocalDateTime.now());
                resourceRatingMapper.updateById(existingRating);
            } else {
                // 创建新评分
                ResourceRating newRating = new ResourceRating();
                newRating.setUserId(userId);
                newRating.setResourceId(resourceId);
                newRating.setRating(rating);
                newRating.setCreatedAt(LocalDateTime.now());
                newRating.setUpdatedAt(LocalDateTime.now());
                resourceRatingMapper.insert(newRating);
            }
        }

        // 计算并返回新的评分统计
        return calculateRatingStatistics(resourceId, userId);
    }

    /**
     * 计算资源的评分统计
     *
     * @param resourceId 资源ID
     * @param userId     用户ID（用于获取用户评分）
     * @return 评分统计结果
     */
    public RatingResult calculateRatingStatistics(Long resourceId, Long userId) {
        // 查询所有评分
        LambdaQueryWrapper<ResourceRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRating::getResourceId, resourceId);
        List<ResourceRating> ratings = resourceRatingMapper.selectList(wrapper);

        int totalRatings = ratings.size();
        double averageRating = 0.0;
        int userRating = 0;

        if (totalRatings > 0) {
            // 计算平均分
            int sum = ratings.stream().mapToInt(ResourceRating::getRating).sum();
            averageRating = (double) sum / totalRatings;

            // 获取用户评分
            if (userId != null) {
                userRating = ratings.stream()
                        .filter(r -> r.getUserId().equals(userId))
                        .findFirst()
                        .map(ResourceRating::getRating)
                        .orElse(0);
            }
        }

        return new RatingResult(averageRating, totalRatings, userRating);
    }

    /**
     * 获取用户对资源的评分
     *
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @return 用户评分（0表示未评分）
     */
    public Integer getUserRating(Long userId, Long resourceId) {
        if (userId == null) {
            return 0;
        }

        LambdaQueryWrapper<ResourceRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRating::getUserId, userId)
                .eq(ResourceRating::getResourceId, resourceId);

        ResourceRating rating = resourceRatingMapper.selectOne(wrapper);
        return rating != null ? rating.getRating() : 0;
    }

    /**
     * 评分统计结果
     */
    public static class RatingResult {
        private final double averageRating;
        private final int totalRatings;
        private final int userRating;

        public RatingResult(double averageRating, int totalRatings, int userRating) {
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
            this.userRating = userRating;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalRatings() {
            return totalRatings;
        }

        public int getUserRating() {
            return userRating;
        }
    }
}
