package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Favorite;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;

    /**
     * 添加收藏
     *
     * @param userId     用户ID
     * @param targetType 对象类型
     * @param targetId   对象ID
     */
    @Transactional
    public void addFavorite(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        if (favoriteMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经收藏过了");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setCreatedAt(LocalDateTime.now());

        favoriteMapper.insert(favorite);
    }

    /**
     * 取消收藏
     *
     * @param userId     用户ID
     * @param targetType 对象类型
     * @param targetId   对象ID
     */
    @Transactional
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        int deleted = favoriteMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException("未找到收藏记录");
        }
    }

    /**
     * 检查用户是否已收藏
     *
     * @param userId     用户ID
     * @param targetType 对象类型
     * @param targetId   对象ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        return favoriteMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取对象的收藏数
     *
     * @param targetType 对象类型
     * @param targetId   对象ID
     * @return 收藏数
     */
    public Long getFavoriteCount(String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        return favoriteMapper.selectCount(wrapper);
    }
}
