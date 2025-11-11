package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    /**
     * 检查用户是否已收藏指定对象
     *
     * @param userId     用户ID
     * @param targetType 对象类型
     * @param targetId   对象ID
     * @return 收藏记录数量
     */
    Long countByUserAndTarget(
            @Param("userId") Long userId,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    /**
     * 获取对象的收藏数
     *
     * @param targetType 对象类型
     * @param targetId   对象ID
     * @return 收藏数
     */
    Long countByTarget(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );
}
