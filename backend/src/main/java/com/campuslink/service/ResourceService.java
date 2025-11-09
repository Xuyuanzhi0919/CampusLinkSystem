package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.resource.*;
import com.campuslink.entity.Resource;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源服务类
 */
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final DownloadLogService downloadLogService;

    /**
     * 上传资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Long uploadResource(Long userId, UploadResourceRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 创建资源实体
        Resource resource = new Resource();
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setUploaderId(userId);
        resource.setFileUrl(request.getFileUrl());
        resource.setFileName(request.getFileName());
        resource.setFileSize(request.getFileSize());
        resource.setFileType(request.getFileType());
        resource.setCategory(request.getCategory());
        resource.setCourseName(request.getCourseName());
        resource.setSchoolId(request.getSchoolId() != null ? request.getSchoolId() : user.getSchoolId());
        resource.setScore(request.getScore());
        resource.setDownloads(0);
        resource.setLikes(0);
        resource.setStatus(0); // 0-待审核
        resource.setCreatedAt(LocalDateTime.now());
        resource.setUpdatedAt(LocalDateTime.now());

        resourceMapper.insert(resource);
        return resource.getRid();
    }

    /**
     * 获取资源列表
     */
    public PageResult<ResourceListResponse> getResourceList(
            String category,
            Long schoolId,
            String keyword,
            Integer page,
            Integer pageSize,
            String sortBy,
            String sortOrder
    ) {
        // 构建分页对象
        Page<Resource> pageObj = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resource::getStatus, 1); // 只查询已通过审核的资源

        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(Resource::getCategory, category);
        }
        if (schoolId != null) {
            queryWrapper.eq(Resource::getSchoolId, schoolId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Resource::getTitle, keyword)
                    .or()
                    .like(Resource::getDescription, keyword)
            );
        }

        // 排序
        if ("downloads".equals(sortBy)) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), Resource::getDownloads);
        } else if ("score".equals(sortBy)) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), Resource::getScore);
        } else {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), Resource::getCreatedAt);
        }

        // 执行分页查询
        IPage<Resource> resourcePage = resourceMapper.selectPage(pageObj, queryWrapper);

        // 转换为DTO
        List<ResourceListResponse> resourceList = resourcePage.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                resourceList,
                resourcePage.getTotal(),
                resourcePage.getCurrent(),
                resourcePage.getSize(),
                resourcePage.getPages()
        );
    }

    /**
     * 获取资源详情
     */
    public ResourceResponse getResourceById(Long resourceId, Long currentUserId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        if (resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        return convertToDetailResponse(resource, currentUserId);
    }

    /**
     * 下载资源
     */
    @Transactional(rollbackFor = Exception.class)
    public DownloadResourceResponse downloadResource(Long resourceId, Long userId) {
        // 查询资源
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查是否已下载过
        boolean hasDownloaded = downloadLogService.hasDownloaded(userId, resourceId);

        int pointsCost = 0;
        if (!hasDownloaded) {
            // 首次下载,需要扣积分
            // 检查积分是否足够
            if (user.getPoints() < resource.getScore()) {
                throw new BusinessException(ResultCode.INSUFFICIENT_POINTS);
            }

            // 扣除积分
            user.setPoints(user.getPoints() - resource.getScore());
            userMapper.updateById(user);
            pointsCost = resource.getScore();

            // 增加下载次数
            resource.setDownloads(resource.getDownloads() + 1);
            resourceMapper.updateById(resource);

            // 给上传者增加积分
            User uploader = userMapper.selectById(resource.getUploaderId());
            if (uploader != null) {
                uploader.setPoints(uploader.getPoints() + resource.getScore());
                userMapper.updateById(uploader);
            }

            // 记录下载日志
            downloadLogService.recordDownload(userId, resourceId, pointsCost);
        }

        // 返回下载链接
        return new DownloadResourceResponse(
                resource.getFileUrl(),
                pointsCost,
                user.getPoints()
        );
    }

    /**
     * 点赞资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer likeResource(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        resource.setLikes(resource.getLikes() + 1);
        resourceMapper.updateById(resource);
        return resource.getLikes();
    }

    /**
     * 取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeResource(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        if (resource.getLikes() > 0) {
            resource.setLikes(resource.getLikes() - 1);
            resourceMapper.updateById(resource);
        }
        return resource.getLikes();
    }

    /**
     * 获取待审核资源列表（管理员）
     */
    public PageResult<ResourceListResponse> getPendingResources(Integer page, Integer pageSize) {
        Page<Resource> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resource::getStatus, 0) // 待审核
                .orderByDesc(Resource::getCreatedAt);

        IPage<Resource> resourcePage = resourceMapper.selectPage(pageObj, queryWrapper);

        List<ResourceListResponse> resourceList = resourcePage.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                resourceList,
                resourcePage.getTotal(),
                resourcePage.getCurrent(),
                resourcePage.getSize(),
                resourcePage.getPages()
        );
    }

    /**
     * 审核通过资源（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveResource(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        if (resource.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该资源已审核,无法重复审核");
        }

        resource.setStatus(1); // 审核通过
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(resource);
    }

    /**
     * 拒绝资源（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectResource(Long resourceId, String rejectReason) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        if (resource.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该资源已审核,无法重复审核");
        }

        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }

        resource.setStatus(2); // 已拒绝
        resource.setRejectReason(rejectReason);
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(resource);
    }

    /**
     * 获取我上传的资源列表
     */
    public PageResult<ResourceListResponse> getMyResources(Long userId, Integer page, Integer pageSize) {
        Page<Resource> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resource::getUploaderId, userId)
                .orderByDesc(Resource::getCreatedAt);

        IPage<Resource> resourcePage = resourceMapper.selectPage(pageObj, queryWrapper);

        List<ResourceListResponse> resourceList = resourcePage.getRecords().stream()
                .map(resource -> {
                    ResourceListResponse response = convertToListResponse(resource);
                    // 对于自己上传的资源,显示审核状态
                    response.setStatus(resource.getStatus());
                    response.setRejectReason(resource.getRejectReason());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageResult<>(
                resourceList,
                resourcePage.getTotal(),
                resourcePage.getCurrent(),
                resourcePage.getSize(),
                resourcePage.getPages()
        );
    }

    /**
     * 转换为列表响应DTO
     */
    private ResourceListResponse convertToListResponse(Resource resource) {
        ResourceListResponse response = new ResourceListResponse();
        response.setResourceId(resource.getRid());
        response.setTitle(resource.getTitle());
        response.setDescription(resource.getDescription());
        response.setFileType(resource.getFileType());
        response.setFileSize(resource.getFileSize());
        response.setCategory(resource.getCategory());
        response.setCourseName(resource.getCourseName());
        response.setScore(resource.getScore());
        response.setDownloads(resource.getDownloads());
        response.setLikes(resource.getLikes());
        response.setCreatedAt(resource.getCreatedAt());

        // 查询上传者信息
        User uploader = userMapper.selectById(resource.getUploaderId());
        if (uploader != null) {
            response.setUploaderName(uploader.getNickname());
            response.setUploaderAvatar(uploader.getAvatarUrl());
        }

        return response;
    }

    /**
     * 转换为详情响应DTO
     */
    private ResourceResponse convertToDetailResponse(Resource resource, Long currentUserId) {
        ResourceResponse response = new ResourceResponse();
        response.setResourceId(resource.getRid());
        response.setTitle(resource.getTitle());
        response.setDescription(resource.getDescription());
        response.setUploaderId(resource.getUploaderId());
        response.setFileUrl(resource.getFileUrl());
        response.setFileName(resource.getFileName());
        response.setFileSize(resource.getFileSize());
        response.setFileType(resource.getFileType());
        response.setCategory(resource.getCategory());
        response.setCourseName(resource.getCourseName());
        response.setSchoolId(resource.getSchoolId());
        response.setScore(resource.getScore());
        response.setDownloads(resource.getDownloads());
        response.setLikes(resource.getLikes());
        response.setCreatedAt(resource.getCreatedAt());

        // 查询上传者信息
        User uploader = userMapper.selectById(resource.getUploaderId());
        if (uploader != null) {
            response.setUploaderName(uploader.getNickname());
            response.setUploaderAvatar(uploader.getAvatarUrl());
        }

        // TODO: 查询学校名称
        // TODO: 查询当前用户是否已下载、是否已点赞
        response.setIsDownloaded(false);
        response.setIsLiked(false);

        return response;
    }
}
