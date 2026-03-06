package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.reward.RedeemRecordVO;
import com.campuslink.dto.reward.RewardItemVO;
import com.campuslink.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分商城控制器
 */
@Tag(name = "积分商城", description = "商品列表、积分兑换、兑换记录等接口")
@RestController
@RequestMapping("/reward")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * 获取商品列表（商品列表公开，无需登录）
     */
    @Operation(summary = "获取商品列表", description = "获取积分商城上架的商品，支持按分类过滤")
    @GetMapping("/items")
    public Result<List<RewardItemVO>> getItemList(
            @Parameter(description = "分类：all | download | privilege | badge | coupon")
            @RequestParam(defaultValue = "all") String category) {
        return Result.success(rewardService.getItemList(category));
    }

    /**
     * 执行积分兑换（需要登录）
     */
    @Operation(summary = "兑换商品", description = "消耗积分兑换指定商品，事务保证原子性")
    @PostMapping("/redeem/{itemId}")
    public Result<RedeemRecordVO> redeemItem(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "商品ID") @PathVariable Long itemId) {
        return Result.success(rewardService.redeemItem(userId, itemId));
    }

    /**
     * 获取我的兑换记录（分页）
     */
    @Operation(summary = "获取兑换记录", description = "分页获取当前用户的积分兑换历史")
    @GetMapping("/records")
    public Result<PageResult<RedeemRecordVO>> getRedeemRecords(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(rewardService.getRedeemRecords(userId, page, pageSize));
    }

    /**
     * 校验用户是否持有指定权益（每次请求校验场景）
     */
    @Operation(summary = "校验权益", description = "检查用户是否拥有有效的指定类型权益")
    @GetMapping("/privilege/check")
    public Result<Boolean> checkPrivilege(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "权益类型，如 vip_trial、extra_download")
            @RequestParam String effectType) {
        return Result.success(rewardService.hasActivePrivilege(userId, effectType));
    }
}
