package com.campuslink.service;

import com.campuslink.common.PageResult;
import com.campuslink.dto.reward.RedeemRecordVO;
import com.campuslink.dto.reward.RewardItemVO;

import java.util.List;

/**
 * 积分商城服务接口
 */
public interface RewardService {

    /**
     * 获取上架商品列表
     *
     * @param category 分类过滤（null 表示全部）
     */
    List<RewardItemVO> getItemList(String category);

    /**
     * 执行积分兑换（事务保证原子性）
     *
     * @param userId 操作用户ID
     * @param itemId 商品ID
     * @return 兑换记录
     */
    RedeemRecordVO redeemItem(Long userId, Long itemId);

    /**
     * 获取用户兑换记录（分页）
     */
    PageResult<RedeemRecordVO> getRedeemRecords(Long userId, Integer page, Integer pageSize);

    /**
     * 校验用户是否拥有指定类型的有效权益（用于每次请求校验）
     *
     * @param userId     用户ID
     * @param effectType 权益类型，如 vip_trial、extra_download
     */
    boolean hasActivePrivilege(Long userId, String effectType);
}
