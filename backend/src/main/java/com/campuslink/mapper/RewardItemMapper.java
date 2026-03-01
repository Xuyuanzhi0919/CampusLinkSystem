package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.RewardItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 积分商品 Mapper
 */
@Mapper
public interface RewardItemMapper extends BaseMapper<RewardItem> {

    /**
     * 库存有限时原子扣减库存（防止超卖）
     */
    @Update("UPDATE reward_items SET stock = stock - 1 WHERE item_id = #{itemId} AND stock > 0")
    int deductStockIfAvailable(@Param("itemId") Long itemId);
}
