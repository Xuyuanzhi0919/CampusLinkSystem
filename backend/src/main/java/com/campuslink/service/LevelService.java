package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.SystemConfig;
import com.campuslink.entity.User;
import com.campuslink.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户等级服务
 * 根据积分和 system_config 中的阈值计算并升级用户等级
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelService {

    private final SystemConfigMapper systemConfigMapper;

    /**
     * 根据积分计算对应等级（从 system_config 读取阈值）
     * 返回最高满足条件的等级，最低为 1
     */
    public int calcLevel(int points) {
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .likeRight(SystemConfig::getConfigKey, "level.threshold_"));

        int level = 1;
        for (SystemConfig cfg : configs) {
            try {
                String key = cfg.getConfigKey(); // e.g. "level.threshold_3"
                int levelNum = Integer.parseInt(key.substring("level.threshold_".length()));
                int threshold = Integer.parseInt(cfg.getConfigValue());
                if (points >= threshold && levelNum > level) {
                    level = levelNum;
                }
            } catch (NumberFormatException ignored) {
                // 忽略非法配置值
            }
        }
        return level;
    }

    /**
     * 检查并升级用户等级（只升不降）
     * 仅修改 user.level，不保存——调用方负责 userMapper.updateById(user)
     *
     * @return true 表示等级发生了升级
     */
    public boolean checkAndUpgrade(User user) {
        int newLevel = calcLevel(user.getPoints() == null ? 0 : user.getPoints());
        if (newLevel > (user.getLevel() == null ? 1 : user.getLevel())) {
            log.info("用户等级升级: userId={}, {} → {}", user.getUId(), user.getLevel(), newLevel);
            user.setLevel(newLevel);
            return true;
        }
        return false;
    }
}
