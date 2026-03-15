package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.dto.FavoriteItemVO;
import com.campuslink.entity.Favorite;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.entity.Task;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.FavoriteMapper;
import com.campuslink.mapper.QuestionMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ResourceMapper resourceMapper;
    private final QuestionMapper questionMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

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

    /**
     * 批量查询用户收藏的资源ID集合
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @return 已收藏的资源ID集合
     */
    public Set<Long> getFavoritedResourceIds(Long userId, Set<Long> resourceIds) {
        if (userId == null || resourceIds == null || resourceIds.isEmpty()) {
            return Set.of();
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, "resource")
                .in(Favorite::getTargetId, resourceIds)
                .select(Favorite::getTargetId);

        return favoriteMapper.selectList(wrapper)
                .stream()
                .map(Favorite::getTargetId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的收藏列表
     *
     * @param userId 用户ID
     * @param targetType 对象类型 (可选, null表示全部)
     * @param page   页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    public PageResult<FavoriteItemVO> getUserFavorites(Long userId, String targetType, Integer page, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);

        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Favorite::getTargetType, targetType);
        } else {
            // 全部收藏时，排除"关注"类型（question_follow），避免混入收藏列表
            wrapper.ne(Favorite::getTargetType, "question_follow");
        }

        wrapper.orderByDesc(Favorite::getCreatedAt);

        // 分页查询
        Page<Favorite> pageParam = new Page<>(page, pageSize);
        Page<Favorite> favoritePage = favoriteMapper.selectPage(pageParam, wrapper);

        // 转换为 VO
        List<FavoriteItemVO> items = new ArrayList<>();
        for (Favorite favorite : favoritePage.getRecords()) {
            FavoriteItemVO item = convertToVO(favorite);
            if (item != null) {
                items.add(item);
            }
        }

        PageResult<FavoriteItemVO> result = new PageResult<>();
        result.setList(items);
        result.setTotal(favoritePage.getTotal());
        result.setPage((long) page);
        result.setPageSize((long) pageSize);
        result.setTotalPages(favoritePage.getPages());
        return result;
    }

    /**
     * 将收藏实体转换为 VO
     */
    private FavoriteItemVO convertToVO(Favorite favorite) {
        String type = favorite.getTargetType();
        Long targetId = favorite.getTargetId();

        FavoriteItemVO.FavoriteItemVOBuilder builder = FavoriteItemVO.builder()
                .favoriteId(favorite.getFavoriteId())
                .targetType(type)
                .targetId(targetId)
                .createdAt(favorite.getCreatedAt());

        try {
            switch (type.toLowerCase()) {
                case "resource":
                    Resource resource = resourceMapper.selectById(targetId);
                    if (resource != null) {
                        User resourceUploader = userMapper.selectById(resource.getUploaderId());
                        builder.title(resource.getTitle())
                                .description(resource.getDescription())
                                .creatorId(resource.getUploaderId())
                                .creatorName(resourceUploader != null ? resourceUploader.getNickname() : "未知用户")
                                .viewCount(resource.getDownloads())
                                .likeCount(resource.getLikes())
                                .targetCreatedAt(resource.getCreatedAt());
                    }
                    break;

                case "question":
                    Question question = questionMapper.selectById(targetId);
                    if (question != null) {
                        User asker = userMapper.selectById(question.getAskerId());
                        builder.title(question.getTitle())
                                .description(question.getContent())
                                .creatorId(question.getAskerId())
                                .creatorName(asker != null ? asker.getNickname() : "未知用户")
                                .viewCount(question.getViews())
                                .likeCount(0) // Question没有likes字段
                                .targetCreatedAt(question.getCreatedAt());
                    }
                    break;

                case "task":
                    Task task = taskMapper.selectById(targetId);
                    if (task != null) {
                        User publisher = userMapper.selectById(task.getPublisherId());
                        builder.title(task.getTitle())
                                .description(task.getContent())
                                .creatorId(task.getPublisherId())
                                .creatorName(publisher != null ? publisher.getNickname() : "未知用户")
                                .viewCount(0) // 任务暂无浏览数
                                .likeCount(0) // 任务暂无点赞数
                                .targetCreatedAt(task.getCreatedAt());
                    }
                    break;

                default:
                    return null;
            }
        } catch (Exception e) {
            // 如果对象已被删除，返回 null
            return null;
        }

        return builder.build();
    }
}
