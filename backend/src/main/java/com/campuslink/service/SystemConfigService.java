package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.config.CreateConfigRequest;
import com.campuslink.dto.config.SystemConfigResponse;
import com.campuslink.dto.config.UpdateConfigRequest;
import com.campuslink.entity.SystemConfig;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务类
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    /**
     * 创建系统配置
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(CreateConfigRequest request) {
        // 检查配置键是否已存在
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, request.getConfigKey());
        Long count = systemConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "配置键已存在");
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        systemConfigMapper.insert(config);
        return config.getConfigId();
    }

    /**
     * 更新系统配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, UpdateConfigRequest request) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "配置不存在");
        }

        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setUpdatedAt(LocalDateTime.now());

        systemConfigMapper.updateById(config);
    }

    /**
     * 删除系统配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "配置不存在");
        }

        systemConfigMapper.deleteById(config.getConfigId());
    }

    /**
     * 根据配置键获取配置
     */
    public SystemConfigResponse getConfigByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "配置不存在");
        }

        return convertToResponse(config);
    }

    /**
     * 获取配置值（不包含其他信息）
     */
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            return null;
        }

        return config.getConfigValue();
    }

    /**
     * 获取所有配置列表
     */
    public List<SystemConfigResponse> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 按前缀搜索配置
     */
    public List<SystemConfigResponse> searchConfigsByPrefix(String prefix) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SystemConfig::getConfigKey, prefix);
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);

        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 批量设置配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSetConfigs(List<CreateConfigRequest> requests) {
        for (CreateConfigRequest request : requests) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, request.getConfigKey());
            SystemConfig existing = systemConfigMapper.selectOne(wrapper);

            if (existing != null) {
                // 配置已存在，更新
                existing.setConfigValue(request.getConfigValue());
                existing.setDescription(request.getDescription());
                existing.setUpdatedAt(LocalDateTime.now());
                systemConfigMapper.updateById(existing);
            } else {
                // 配置不存在，创建
                SystemConfig config = new SystemConfig();
                config.setConfigKey(request.getConfigKey());
                config.setConfigValue(request.getConfigValue());
                config.setDescription(request.getDescription());
                config.setCreatedAt(LocalDateTime.now());
                config.setUpdatedAt(LocalDateTime.now());
                systemConfigMapper.insert(config);
            }
        }
    }

    /**
     * 转换为响应DTO
     */
    private SystemConfigResponse convertToResponse(SystemConfig config) {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setConfigId(config.getConfigId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setDescription(config.getDescription());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}
