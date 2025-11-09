package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.school.CreateSchoolRequest;
import com.campuslink.dto.school.SchoolResponse;
import com.campuslink.dto.school.UpdateSchoolRequest;
import com.campuslink.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学校控制器
 */
@Tag(name = "学校管理", description = "学校信息的增删改查接口")
@RestController
@RequestMapping("/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * 创建学校(管理员)
     */
    @Operation(summary = "创建学校", description = "管理员创建新学校")
    @PostMapping
    public Result<Map<String, Long>> createSchool(
            @Valid @RequestBody CreateSchoolRequest request,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        Long schoolId = schoolService.createSchool(request);
        return Result.success("创建成功", Map.of("schoolId", schoolId));
    }

    /**
     * 更新学校信息(管理员)
     */
    @Operation(summary = "更新学校信息", description = "管理员更新学校信息")
    @PutMapping("/{id}")
    public Result<Void> updateSchool(
            @Parameter(description = "学校ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSchoolRequest request,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        schoolService.updateSchool(id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除学校(管理员)
     */
    @Operation(summary = "删除学校", description = "管理员删除学校")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchool(
            @Parameter(description = "学校ID") @PathVariable Long id,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        schoolService.deleteSchool(id);
        return Result.success("删除成功");
    }

    /**
     * 获取学校详情
     */
    @Operation(summary = "获取学校详情", description = "根据ID获取学校详细信息")
    @GetMapping("/{id}")
    public Result<SchoolResponse> getSchoolById(
            @Parameter(description = "学校ID") @PathVariable Long id
    ) {
        SchoolResponse school = schoolService.getSchoolById(id);
        return Result.success(school);
    }

    /**
     * 获取学校列表(分页)
     */
    @Operation(summary = "获取学校列表", description = "分页查询学校列表,支持筛选")
    @GetMapping("/list")
    public Result<PageResult<SchoolResponse>> getSchoolList(
            @Parameter(description = "省份") @RequestParam(required = false) String province,
            @Parameter(description = "城市") @RequestParam(required = false) String city,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<SchoolResponse> result = schoolService.getSchoolList(
                province, city, keyword, status, page, pageSize
        );
        return Result.success(result);
    }

    /**
     * 获取所有学校(不分页,用于下拉选择)
     */
    @Operation(summary = "获取所有学校", description = "获取所有正常状态的学校,不分页,用于下拉选择")
    @GetMapping("/all")
    public Result<List<SchoolResponse>> getAllSchools() {
        List<SchoolResponse> schools = schoolService.getAllSchools();
        return Result.success(schools);
    }

    /**
     * 按省份获取学校列表
     */
    @Operation(summary = "按省份获取学校", description = "根据省份获取该省所有学校")
    @GetMapping("/by-province")
    public Result<List<SchoolResponse>> getSchoolsByProvince(
            @Parameter(description = "省份") @RequestParam String province
    ) {
        List<SchoolResponse> schools = schoolService.getSchoolsByProvince(province);
        return Result.success(schools);
    }

    /**
     * 按城市获取学校列表
     */
    @Operation(summary = "按城市获取学校", description = "根据城市获取该城市所有学校")
    @GetMapping("/by-city")
    public Result<List<SchoolResponse>> getSchoolsByCity(
            @Parameter(description = "城市") @RequestParam String city
    ) {
        List<SchoolResponse> schools = schoolService.getSchoolsByCity(city);
        return Result.success(schools);
    }
}
