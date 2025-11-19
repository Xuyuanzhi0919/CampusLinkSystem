package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.dto.download.DownloadLogResponse;
import com.campuslink.entity.DownloadLog;
import com.campuslink.entity.Resource;
import com.campuslink.entity.User;
import com.campuslink.mapper.DownloadLogMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 下载记录服务类
 */
@Service
@RequiredArgsConstructor
public class DownloadLogService {

    private final DownloadLogMapper downloadLogMapper;
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;

    /**
     * 记录下载
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(Long userId, Long resourceId, Integer pointsCost) {
        DownloadLog log = new DownloadLog();
        log.setUserId(userId);
        log.setResourceId(resourceId);
        log.setPointsCost(pointsCost);
        log.setCreatedAt(LocalDateTime.now());

        downloadLogMapper.insert(log);
    }

    /**
     * 检查用户是否已下载过该资源
     */
    public boolean hasDownloaded(Long userId, Long resourceId) {
        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getUserId, userId)
                .eq(DownloadLog::getResourceId, resourceId);

        return downloadLogMapper.selectCount(wrapper) > 0;
    }

    /**
     * 批量查询用户已下载的资源ID集合
     * 用于在资源列表中标记已下载状态，避免N+1查询问题
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @return 已下载的资源ID集合
     */
    public Set<Long> getDownloadedResourceIds(Long userId, Set<Long> resourceIds) {
        if (userId == null || resourceIds == null || resourceIds.isEmpty()) {
            return Set.of();
        }

        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getUserId, userId)
                .in(DownloadLog::getResourceId, resourceIds)
                .select(DownloadLog::getResourceId);

        return downloadLogMapper.selectList(wrapper)
                .stream()
                .map(DownloadLog::getResourceId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的下载历史
     */
    public PageResult<DownloadLogResponse> getMyDownloadHistory(
            Long userId,
            Integer page,
            Integer pageSize
    ) {
        Page<DownloadLog> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getUserId, userId)
                .orderByDesc(DownloadLog::getCreatedAt);

        IPage<DownloadLog> logPage = downloadLogMapper.selectPage(pageObj, wrapper);

        List<DownloadLogResponse> logList = logPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                logList,
                logPage.getTotal(),
                logPage.getCurrent(),
                logPage.getSize(),
                logPage.getPages()
        );
    }

    /**
     * 获取资源的下载记录(管理员)
     */
    public PageResult<DownloadLogResponse> getResourceDownloadHistory(
            Long resourceId,
            Integer page,
            Integer pageSize
    ) {
        Page<DownloadLog> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getResourceId, resourceId)
                .orderByDesc(DownloadLog::getCreatedAt);

        IPage<DownloadLog> logPage = downloadLogMapper.selectPage(pageObj, wrapper);

        List<DownloadLogResponse> logList = logPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                logList,
                logPage.getTotal(),
                logPage.getCurrent(),
                logPage.getSize(),
                logPage.getPages()
        );
    }

    /**
     * 获取用户的下载统计
     */
    public Long getDownloadCount(Long userId) {
        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getUserId, userId);
        return downloadLogMapper.selectCount(wrapper);
    }

    /**
     * 获取资源的下载统计
     */
    public Long getResourceDownloadCount(Long resourceId) {
        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getResourceId, resourceId);
        return downloadLogMapper.selectCount(wrapper);
    }

    /**
     * 转换为响应DTO
     */
    private DownloadLogResponse convertToResponse(DownloadLog log) {
        DownloadLogResponse response = new DownloadLogResponse();
        response.setDlId(log.getDlId());
        response.setUserId(log.getUserId());
        response.setResourceId(log.getResourceId());
        response.setPointsCost(log.getPointsCost());
        response.setCreatedAt(log.getCreatedAt());

        // 查询用户信息
        User user = userMapper.selectById(log.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname());
            response.setUserAvatar(user.getAvatarUrl());
        }

        // 查询资源信息
        Resource resource = resourceMapper.selectById(log.getResourceId());
        if (resource != null) {
            response.setResourceTitle(resource.getTitle());
            response.setFileType(resource.getFileType());
        }

        return response;
    }
}
