package com.campuslink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.dto.reward.RedeemRecordVO;
import com.campuslink.dto.reward.RewardItemVO;
import com.campuslink.entity.PointsLog;
import com.campuslink.entity.RedeemRecord;
import com.campuslink.entity.RewardItem;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.PointsLogMapper;
import com.campuslink.mapper.RedeemRecordMapper;
import com.campuslink.mapper.RewardItemMapper;
import com.campuslink.mapper.UserMapper;
import com.campuslink.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分商城服务实现
 */
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final RewardItemMapper rewardItemMapper;
    private final RedeemRecordMapper redeemRecordMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    public List<RewardItemVO> getItemList(String category) {
        LambdaQueryWrapper<RewardItem> wrapper = new LambdaQueryWrapper<RewardItem>()
                .eq(RewardItem::getStatus, 1)
                .eq(category != null && !category.equals("all"), RewardItem::getCategory, category)
                .orderByAsc(RewardItem::getSort);

        return rewardItemMapper.selectList(wrapper).stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedeemRecordVO redeemItem(Long userId, Long itemId) {
        // 1. 校验商品
        RewardItem item = rewardItemMapper.selectById(itemId);
        if (item == null || item.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (item.getStock() == 0) {
            throw new BusinessException("商品库存不足，请选择其他商品");
        }

        // 2. 原子扣减积分（UPDATE ... WHERE points >= cost），返回 0 表示积分不足
        int affected = userMapper.deductPointsIfEnough(userId, item.getPointsCost());
        if (affected == 0) {
            throw new BusinessException("积分不足，当前积分无法兑换该商品");
        }

        // 3. 查询扣减后的剩余积分（用于日志）
        User user = userMapper.selectById(userId);
        int pointsAfter = user.getPoints();

        // 4. 写入积分变动日志
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPointsChange(-item.getPointsCost());
        log.setPointsAfter(pointsAfter);
        log.setReason("积分兑换：" + item.getName());
        log.setRelatedType("reward");
        log.setRelatedId(itemId);
        log.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(log);

        // 5. 有限库存时原子扣减（防止超卖）
        if (item.getStock() != -1) {
            int stockAffected = rewardItemMapper.deductStockIfAvailable(itemId);
            if (stockAffected == 0) {
                // 并发场景下库存刚好耗尽，回滚
                throw new BusinessException("商品库存不足，兑换失败");
            }
        }

        // 6. 创建兑换记录
        RedeemRecord record = new RedeemRecord();
        record.setUserId(userId);
        record.setItemId(itemId);
        record.setItemName(item.getName());
        record.setPointsCost(item.getPointsCost());
        record.setEffectType(item.getEffectType());
        record.setEffectValue(item.getEffectValue());
        record.setStatus(1); // 虚拟商品立即发放
        record.setCreatedAt(LocalDateTime.now());
        redeemRecordMapper.insert(record);

        return toRecordVO(record);
    }

    @Override
    public PageResult<RedeemRecordVO> getRedeemRecords(Long userId, Integer page, Integer pageSize) {
        Page<RedeemRecord> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<RedeemRecord> wrapper = new LambdaQueryWrapper<RedeemRecord>()
                .eq(RedeemRecord::getUserId, userId)
                .orderByDesc(RedeemRecord::getCreatedAt);

        Page<RedeemRecord> result = redeemRecordMapper.selectPage(pageObj, wrapper);

        List<RedeemRecordVO> list = result.getRecords().stream()
                .map(this::toRecordVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, result.getTotal(), page, pageSize);
    }

    @Override
    public boolean hasActivePrivilege(Long userId, String effectType) {
        return redeemRecordMapper.countActivePrivilege(userId, effectType) > 0;
    }

    // ── 私有转换方法 ──────────────────────────────────────────

    private RewardItemVO toItemVO(RewardItem item) {
        RewardItemVO vo = new RewardItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private RedeemRecordVO toRecordVO(RedeemRecord record) {
        RedeemRecordVO vo = new RedeemRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
