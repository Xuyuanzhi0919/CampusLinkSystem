package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.school.CreateSchoolRequest;
import com.campuslink.dto.school.SchoolResponse;
import com.campuslink.dto.school.UpdateSchoolRequest;
import com.campuslink.entity.School;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.SchoolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校服务类
 */
@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolMapper schoolMapper;

    /**
     * 创建学校
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSchool(CreateSchoolRequest request) {
        // 检查学校名称是否已存在
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSchoolName, request.getSchoolName());
        Long count = schoolMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "学校名称已存在");
        }

        School school = new School();
        school.setSchoolName(request.getSchoolName());
        school.setProvince(request.getProvince());
        school.setCity(request.getCity());
        school.setAddress(request.getAddress());
        school.setLogoUrl(request.getLogoUrl());
        school.setStatus(1); // 默认正常状态
        school.setCreatedAt(LocalDateTime.now());

        schoolMapper.insert(school);
        return school.getSchoolId();
    }

    /**
     * 更新学校信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSchool(Long schoolId, UpdateSchoolRequest request) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "学校不存在");
        }

        // 如果要修改学校名称,检查是否与其他学校重名
        if (request.getSchoolName() != null && !request.getSchoolName().equals(school.getSchoolName())) {
            LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(School::getSchoolName, request.getSchoolName())
                    .ne(School::getSchoolId, schoolId);
            Long count = schoolMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "学校名称已存在");
            }
            school.setSchoolName(request.getSchoolName());
        }

        if (request.getProvince() != null) {
            school.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            school.setCity(request.getCity());
        }
        if (request.getAddress() != null) {
            school.setAddress(request.getAddress());
        }
        if (request.getLogoUrl() != null) {
            school.setLogoUrl(request.getLogoUrl());
        }
        if (request.getStatus() != null) {
            school.setStatus(request.getStatus());
        }

        schoolMapper.updateById(school);
    }

    /**
     * 删除学校
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchool(Long schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "学校不存在");
        }

        // TODO: 检查是否有用户关联此学校,如果有则不允许删除
        schoolMapper.deleteById(schoolId);
    }

    /**
     * 获取学校详情
     */
    public SchoolResponse getSchoolById(Long schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "学校不存在");
        }
        return convertToResponse(school);
    }

    /**
     * 获取学校列表
     */
    public PageResult<SchoolResponse> getSchoolList(
            String province,
            String city,
            String keyword,
            Integer status,
            Integer page,
            Integer pageSize
    ) {
        Page<School> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();

        if (province != null && !province.isEmpty()) {
            wrapper.eq(School::getProvince, province);
        }
        if (city != null && !city.isEmpty()) {
            wrapper.eq(School::getCity, city);
        }
        if (status != null) {
            wrapper.eq(School::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(School::getSchoolName, keyword)
                    .or()
                    .like(School::getAddress, keyword)
            );
        }

        wrapper.orderByDesc(School::getCreatedAt);

        IPage<School> schoolPage = schoolMapper.selectPage(pageObj, wrapper);

        List<SchoolResponse> schoolList = schoolPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                schoolList,
                schoolPage.getTotal(),
                schoolPage.getCurrent(),
                schoolPage.getSize(),
                schoolPage.getPages()
        );
    }

    /**
     * 获取所有学校列表(不分页,用于下拉选择)
     */
    public List<SchoolResponse> getAllSchools() {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getStatus, 1) // 只返回正常状态的学校
                .orderBy(true, true, School::getSchoolName);

        List<School> schools = schoolMapper.selectList(wrapper);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 按省份获取学校列表
     */
    public List<SchoolResponse> getSchoolsByProvince(String province) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getProvince, province)
                .eq(School::getStatus, 1)
                .orderBy(true, true, School::getSchoolName);

        List<School> schools = schoolMapper.selectList(wrapper);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 按城市获取学校列表
     */
    public List<SchoolResponse> getSchoolsByCity(String city) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getCity, city)
                .eq(School::getStatus, 1)
                .orderBy(true, true, School::getSchoolName);

        List<School> schools = schoolMapper.selectList(wrapper);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应DTO
     */
    private SchoolResponse convertToResponse(School school) {
        SchoolResponse response = new SchoolResponse();
        response.setSchoolId(school.getSchoolId());
        response.setSchoolName(school.getSchoolName());
        response.setProvince(school.getProvince());
        response.setCity(school.getCity());
        response.setAddress(school.getAddress());
        response.setLogoUrl(school.getLogoUrl());
        response.setStatus(school.getStatus());
        response.setCreatedAt(school.getCreatedAt());
        return response;
    }
}
