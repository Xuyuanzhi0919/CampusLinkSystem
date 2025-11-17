package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.ResourceLike;
import com.campuslink.mapper.ResourceLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源点赞服务类
 */
@Service
@RequiredArgsConstructor
public class ResourceLikeService {

    private final ResourceLikeMapper resourceLikeMapper;

    /**
     * 记录点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLike(Long userId, Long resourceId) {
        ResourceLike like = new ResourceLike();
        like.setUserId(userId);
        like.setResourceId(resourceId);
        like.setCreatedAt(LocalDateTime.now());

        resourceLikeMapper.insert(like);
    }

    /**
     * 删除点赞记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeLike(Long userId, Long resourceId) {
        LambdaQueryWrapper<ResourceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceLike::getUserId, userId)
                .eq(ResourceLike::getResourceId, resourceId);

        resourceLikeMapper.delete(wrapper);
    }

    /**
     * 检查用户是否已点赞该资源
     */
    public boolean hasLiked(Long userId, Long resourceId) {
        LambdaQueryWrapper<ResourceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceLike::getUserId, userId)
                .eq(ResourceLike::getResourceId, resourceId);

        return resourceLikeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 批量查询用户已点赞的资源ID集合
     * 用于在资源列表中标记已点赞状态，避免N+1查询问题
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @return 已点赞的资源ID集合
     */
    public Set<Long> getLikedResourceIds(Long userId, Set<Long> resourceIds) {
        if (userId == null || resourceIds == null || resourceIds.isEmpty()) {
            return Set.of();
        }

        LambdaQueryWrapper<ResourceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceLike::getUserId, userId)
                .in(ResourceLike::getResourceId, resourceIds)
                .select(ResourceLike::getResourceId);

        return resourceLikeMapper.selectList(wrapper)
                .stream()
                .map(ResourceLike::getResourceId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取资源的点赞数
     */
    public Long getResourceLikeCount(Long resourceId) {
        LambdaQueryWrapper<ResourceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceLike::getResourceId, resourceId);
        return resourceLikeMapper.selectCount(wrapper);
    }

    /**
     * 获取用户的点赞总数
     */
    public Long getUserLikeCount(Long userId) {
        LambdaQueryWrapper<ResourceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceLike::getUserId, userId);
        return resourceLikeMapper.selectCount(wrapper);
    }
}
