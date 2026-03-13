package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campuslink.common.Result;
import com.campuslink.entity.SystemConfig;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.SystemConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员系统配置接口
 */
@Tag(name = "管理端 - 系统配置")
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SystemConfigMapper systemConfigMapper;

    @Operation(summary = "配置列表")
    @GetMapping
    public Result<List<SystemConfig>> listConfigs() {
        List<SystemConfig> list = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigKey));
        return Result.success(list);
    }

    @Operation(summary = "新增配置")
    @PostMapping
    public Result<Void> createConfig(@RequestBody SystemConfig config) {
        if (config.getConfigKey() == null || config.getConfigKey().isBlank())
            throw new BusinessException(400, "配置键不能为空");
        Long exists = systemConfigMapper.selectCount(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, config.getConfigKey()));
        if (exists > 0) throw new BusinessException(400, "配置键已存在");
        systemConfigMapper.insert(config);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新配置", description = "按 configKey 更新值和描述")
    @PutMapping("/{configKey}")
    public Result<Void> updateConfig(
            @PathVariable String configKey,
            @RequestBody Map<String, String> body) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, configKey));
        if (config == null) throw new BusinessException(404, "配置不存在");
        String value = body.get("configValue");
        if (value != null) config.setConfigValue(value);
        String desc = body.get("description");
        if (desc != null) config.setDescription(desc);
        systemConfigMapper.updateById(config);
        return Result.success("更新成功");
    }

    @Operation(summary = "批量更新配置", description = "仅更新已存在的 key，不新增不删除，管理后台分组保存用")
    @PutMapping("/batch")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> updates) {
        if (updates == null || updates.isEmpty()) return Result.success("无需更新");
        updates.forEach((key, value) -> systemConfigMapper.update(null,
                new LambdaUpdateWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key)
                        .set(SystemConfig::getConfigValue, value)
                        .set(SystemConfig::getUpdatedAt, LocalDateTime.now())));
        return Result.success("配置已保存");
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{configKey}")
    public Result<Void> deleteConfig(@PathVariable String configKey) {
        int deleted = systemConfigMapper.delete(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, configKey));
        if (deleted == 0) throw new BusinessException(404, "配置不存在");
        return Result.success("删除成功");
    }
}
