package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.RedeemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 兑换记录 Mapper
 */
@Mapper
public interface RedeemRecordMapper extends BaseMapper<RedeemRecord> {

    /**
     * 查询用户是否拥有指定类型的有效权益（用于每次请求校验）
     */
    @Select("SELECT COUNT(*) FROM redeem_records " +
            "WHERE user_id = #{userId} AND effect_type = #{effectType} AND status = 1")
    int countActivePrivilege(@Param("userId") Long userId, @Param("effectType") String effectType);
}
