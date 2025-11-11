package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.TaskRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 任务评价 Mapper
 */
@Mapper
public interface TaskRatingMapper extends BaseMapper<TaskRating> {

    /**
     * 获取用户的平均评分
     */
    @Select("SELECT AVG(rating) FROM task_rating WHERE rated_id = #{userId}")
    Double getAverageRating(@Param("userId") Long userId);

    /**
     * 获取用户被评价次数
     */
    @Select("SELECT COUNT(*) FROM task_rating WHERE rated_id = #{userId}")
    Long getRatingCount(@Param("userId") Long userId);
}
