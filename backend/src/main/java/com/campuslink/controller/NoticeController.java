package com.campuslink.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.Result;
import com.campuslink.entity.ScheduledNotice;
import com.campuslink.mapper.ScheduledNoticeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 校园公告控制器（用户端，无需登录）
 */
@Tag(name = "校园公告", description = "获取面向全体用户的校园公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final ScheduledNoticeMapper scheduledNoticeMapper;

    /** 公告 VO */
    @Data
    static class NoticeVO {
        private Long id;
        private String title;
        private String content;
        private String publishedAt;
        private boolean isNew;
    }

    @Operation(summary = "获取最新公告列表", description = "返回已发送的全体公告，按发送时间倒序")
    @GetMapping("/public")
    public Result<List<NoticeVO>> getPublicNotices(
            @RequestParam(defaultValue = "10") Integer limit) {

        List<ScheduledNotice> notices = scheduledNoticeMapper.selectList(
                new LambdaQueryWrapper<ScheduledNotice>()
                        .eq(ScheduledNotice::getStatus, 1)           // 已发送
                        .in(ScheduledNotice::getTargetType, "all", "role") // 全体或角色广播
                        .orderByDesc(ScheduledNotice::getScheduledAt)
                        .last("LIMIT " + limit)
        );

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<NoticeVO> result = notices.stream().map(n -> {
            NoticeVO vo = new NoticeVO();
            vo.setId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setPublishedAt(n.getScheduledAt() != null ? n.getScheduledAt().format(fmt) : "");
            vo.setNew(n.getScheduledAt() != null && n.getScheduledAt().isAfter(threeDaysAgo));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
