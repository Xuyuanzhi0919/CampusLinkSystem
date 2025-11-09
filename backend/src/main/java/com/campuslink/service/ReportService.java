package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.report.CreateReportRequest;
import com.campuslink.dto.report.HandleReportRequest;
import com.campuslink.dto.report.ReportResponse;
import com.campuslink.entity.*;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 举报服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final CommentMapper commentMapper;

    // 举报类型映射
    private static final Map<Integer, String> REPORT_TYPE_MAP = new HashMap<>();
    // 举报原因类型映射
    private static final Map<Integer, String> REASON_TYPE_MAP = new HashMap<>();
    // 处理状态映射
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        REPORT_TYPE_MAP.put(1, "帖子");
        REPORT_TYPE_MAP.put(2, "评论");
        REPORT_TYPE_MAP.put(3, "用户");
        REPORT_TYPE_MAP.put(4, "活动");

        REASON_TYPE_MAP.put(1, "垃圾信息");
        REASON_TYPE_MAP.put(2, "违规内容");
        REASON_TYPE_MAP.put(3, "骚扰辱骂");
        REASON_TYPE_MAP.put(4, "侵权");
        REASON_TYPE_MAP.put(5, "其他");

        STATUS_MAP.put(0, "待处理");
        STATUS_MAP.put(1, "已处理");
        STATUS_MAP.put(2, "已驳回");
    }

    /**
     * 创建举报
     */
    @Transactional
    public Long createReport(Long userId, CreateReportRequest request) {
        // 验证被举报对象是否存在
        validateTargetExists(request.getReportType(), request.getTargetId());

        // 检查是否已经举报过
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, userId)
                .eq(Report::getReportType, request.getReportType())
                .eq(Report::getTargetId, request.getTargetId())
                .eq(Report::getStatus, 0);

        Long existingCount = reportMapper.selectCount(wrapper);
        if (existingCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已经举报过该内容,请等待处理");
        }

        // 创建举报
        Report report = new Report();
        report.setReporterId(userId);
        report.setReportType(request.getReportType());
        report.setTargetId(request.getTargetId());
        report.setReasonType(request.getReasonType());
        report.setDescription(request.getDescription());
        report.setEvidenceImages(request.getEvidenceImages());
        report.setStatus(0); // 待处理
        report.setCreatedAt(LocalDateTime.now());

        reportMapper.insert(report);

        log.info("用户 {} 举报了 {} ID:{}", userId, REPORT_TYPE_MAP.get(request.getReportType()), request.getTargetId());
        return report.getReportId();
    }

    /**
     * 获取举报列表(管理员)
     */
    public PageResult<ReportResponse> getReportList(Integer status, Integer reportType, Integer page, Integer pageSize) {
        Page<Report> reportPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (reportType != null) {
            wrapper.eq(Report::getReportType, reportType);
        }
        wrapper.orderByAsc(Report::getStatus)  // 待处理的优先
                .orderByDesc(Report::getCreatedAt);

        reportPage = reportMapper.selectPage(reportPage, wrapper);

        List<ReportResponse> reportResponses = reportPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                reportResponses,
                reportPage.getTotal(),
                reportPage.getCurrent(),
                reportPage.getSize(),
                reportPage.getPages()
        );
    }

    /**
     * 获取我的举报列表
     */
    public PageResult<ReportResponse> getMyReports(Long userId, Integer page, Integer pageSize) {
        Page<Report> reportPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, userId)
                .orderByDesc(Report::getCreatedAt);

        reportPage = reportMapper.selectPage(reportPage, wrapper);

        List<ReportResponse> reportResponses = reportPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                reportResponses,
                reportPage.getTotal(),
                reportPage.getCurrent(),
                reportPage.getSize(),
                reportPage.getPages()
        );
    }

    /**
     * 获取举报详情
     */
    public ReportResponse getReportDetail(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }

        return convertToResponse(report);
    }

    /**
     * 处理举报(管理员)
     */
    @Transactional
    public void handleReport(Long handlerId, Long reportId, HandleReportRequest request) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }

        if (report.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该举报已经处理过了");
        }

        // 更新举报状态
        report.setStatus(request.getStatus());
        report.setHandleResult(request.getHandleResult());
        report.setHandlerId(handlerId);
        report.setHandledAt(LocalDateTime.now());

        reportMapper.updateById(report);

        log.info("管理员 {} 处理了举报 {}, 结果: {}", handlerId, reportId, STATUS_MAP.get(request.getStatus()));
    }

    /**
     * 删除举报(用户撤销自己的举报)
     */
    @Transactional
    public void deleteReport(Long userId, Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }

        // 只能删除自己的举报
        if (!report.getReporterId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 只能删除待处理的举报
        if (report.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已处理的举报无法撤销");
        }

        reportMapper.deleteById(reportId);
        log.info("用户 {} 撤销了举报 {}", userId, reportId);
    }

    /**
     * 获取待处理举报数量(管理员)
     */
    public Integer getPendingCount() {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0);
        return Math.toIntExact(reportMapper.selectCount(wrapper));
    }

    /**
     * 验证被举报对象是否存在
     */
    private void validateTargetExists(Integer reportType, Long targetId) {
        boolean exists = false;
        String targetName = "";

        switch (reportType) {
            case 1: // 帖子
                // 帖子功能暂未实现,先跳过验证
                exists = true;
                targetName = "帖子";
                break;
            case 2: // 评论
                exists = commentMapper.selectById(targetId) != null;
                targetName = "评论";
                break;
            case 3: // 用户
                exists = userMapper.selectById(targetId) != null;
                targetName = "用户";
                break;
            case 4: // 活动
                exists = activityMapper.selectById(targetId) != null;
                targetName = "活动";
                break;
        }

        if (!exists) {
            throw new BusinessException(ResultCode.NOT_FOUND, targetName + "不存在");
        }
    }

    /**
     * 转换为响应对象
     */
    private ReportResponse convertToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        BeanUtils.copyProperties(report, response);

        // 设置类型名称
        response.setReportTypeName(REPORT_TYPE_MAP.get(report.getReportType()));
        response.setReasonTypeName(REASON_TYPE_MAP.get(report.getReasonType()));
        response.setStatusName(STATUS_MAP.get(report.getStatus()));

        // 查询举报者信息
        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            response.setReporterName(reporter.getNickname());
            response.setReporterAvatar(reporter.getAvatarUrl());
        }

        // 查询处理人信息
        if (report.getHandlerId() != null) {
            User handler = userMapper.selectById(report.getHandlerId());
            if (handler != null) {
                response.setHandlerName(handler.getNickname());
            }
        }

        // 获取被举报对象的内容摘要
        String targetContent = getTargetContent(report.getReportType(), report.getTargetId());
        response.setTargetContent(targetContent);

        return response;
    }

    /**
     * 获取被举报对象的内容摘要
     */
    private String getTargetContent(Integer reportType, Long targetId) {
        try {
            switch (reportType) {
                case 1: // 帖子
                    // 帖子功能暂未实现
                    return "帖子ID: " + targetId;
                case 2: // 评论
                    Comment comment = commentMapper.selectById(targetId);
                    if (comment != null) {
                        return truncate(comment.getContent(), 50);
                    }
                    return "评论ID: " + targetId;
                case 3: // 用户
                    User user = userMapper.selectById(targetId);
                    if (user != null) {
                        return user.getNickname();
                    }
                    break;
                case 4: // 活动
                    Activity activity = activityMapper.selectById(targetId);
                    if (activity != null) {
                        return truncate(activity.getTitle(), 50);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("获取被举报对象内容失败: reportType={}, targetId={}", reportType, targetId, e);
        }
        return "内容已删除或不可见";
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
