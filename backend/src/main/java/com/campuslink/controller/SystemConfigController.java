package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.config.CreateConfigRequest;
import com.campuslink.dto.config.SystemConfigResponse;
import com.campuslink.dto.config.UpdateConfigRequest;
import com.campuslink.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置管理", description = "系统配置的增删改查接口（仅管理员）")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /** 公开配置（无需登录）：返回前端页面渲染所需的非敏感配置项 */
    @Operation(summary = "获取公开配置", description = "返回前端渲染所需的非敏感配置（无需登录）")
    @GetMapping("/public")
    public Result<Map<String, String>> getPublicConfigs() {
        String[] publicKeys = {
            "points.daily_signin",
            "points.ask_question",
            "points.answer_question",
            "points.answer_accepted",
            "upload.allowed_types",
            "upload.max_file_size",
            "upload.image_max_size"
        };
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (String key : publicKeys) {
            String value = systemConfigService.getConfigValue(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return Result.success(result);
    }

    /**
     * 创建系统配置
     */
    @Operation(summary = "创建系统配置", description = "创建新的系统配置项（仅管理员）")
    @PostMapping
    public Result<Map<String, Long>> createConfig(
            @Valid @RequestBody CreateConfigRequest request,
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        Long configId = systemConfigService.createConfig(request);
        return Result.success("创建成功", Map.of("configId", configId));
    }

    /**
     * 更新系统配置
     */
    @Operation(summary = "更新系统配置", description = "更新指定配置项（仅管理员）")
    @PutMapping("/{configKey}")
    public Result<Void> updateConfig(
            @Parameter(description = "配置键") @PathVariable String configKey,
            @Valid @RequestBody UpdateConfigRequest request,
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        systemConfigService.updateConfig(configKey, request);
        return Result.success("更新成功");
    }

    /**
     * 删除系统配置
     */
    @Operation(summary = "删除系统配置", description = "删除指定配置项（仅管理员）")
    @DeleteMapping("/{configKey}")
    public Result<Void> deleteConfig(
            @Parameter(description = "配置键") @PathVariable String configKey,
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        systemConfigService.deleteConfig(configKey);
        return Result.success("删除成功");
    }

    /**
     * 获取配置详情
     */
    @Operation(summary = "获取配置详情", description = "根据配置键获取配置详情")
    @GetMapping("/{configKey}")
    public Result<SystemConfigResponse> getConfig(
            @Parameter(description = "配置键") @PathVariable String configKey
    ) {
        SystemConfigResponse response = systemConfigService.getConfigByKey(configKey);
        return Result.success(response);
    }

    /**
     * 获取配置值
     */
    @Operation(summary = "获取配置值", description = "仅返回配置值，不包含其他信息")
    @GetMapping("/{configKey}/value")
    public Result<Map<String, String>> getConfigValue(
            @Parameter(description = "配置键") @PathVariable String configKey
    ) {
        String value = systemConfigService.getConfigValue(configKey);
        return Result.success(Map.of("value", value != null ? value : ""));
    }

    /**
     * 获取所有配置
     */
    @Operation(summary = "获取所有配置", description = "获取系统所有配置项列表（仅管理员）")
    @GetMapping("/list")
    public Result<List<SystemConfigResponse>> getAllConfigs(
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        List<SystemConfigResponse> configs = systemConfigService.getAllConfigs();
        return Result.success(configs);
    }

    /**
     * 搜索配置
     */
    @Operation(summary = "搜索配置", description = "按配置键前缀搜索配置（仅管理员）")
    @GetMapping("/search")
    public Result<List<SystemConfigResponse>> searchConfigs(
            @Parameter(description = "配置键前缀") @RequestParam String prefix,
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        List<SystemConfigResponse> configs = systemConfigService.searchConfigsByPrefix(prefix);
        return Result.success(configs);
    }

    /**
     * 批量设置配置
     */
    @Operation(summary = "批量设置配置", description = "批量创建或更新配置项（仅管理员）")
    @PostMapping("/batch")
    public Result<Void> batchSetConfigs(
            @Valid @RequestBody List<CreateConfigRequest> requests,
            @RequestAttribute("role") String role
    ) {
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可操作");
        }

        systemConfigService.batchSetConfigs(requests);
        return Result.success("批量设置成功");
    }
}
