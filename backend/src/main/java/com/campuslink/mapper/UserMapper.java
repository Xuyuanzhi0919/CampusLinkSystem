package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 原子扣减积分（积分足够时才执行），返回影响行数
     * 用于兑换场景，避免并发超扣
     */
    @Update("UPDATE user SET points = points - #{cost} WHERE u_id = #{userId} AND points >= #{cost}")
    int deductPointsIfEnough(@Param("userId") Long userId, @Param("cost") Integer cost);
}
