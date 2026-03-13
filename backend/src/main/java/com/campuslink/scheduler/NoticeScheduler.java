package com.campuslink.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.entity.ScheduledNotice;
import com.campuslink.mapper.ScheduledNoticeMapper;
import com.campuslink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时公告调度器：每分钟检查待发送的定时公告
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeScheduler {

    private final ScheduledNoticeMapper scheduledNoticeMapper;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    public void processScheduledNotices() {
        List<ScheduledNotice> pending = scheduledNoticeMapper.selectList(
                new LambdaQueryWrapper<ScheduledNotice>()
                        .eq(ScheduledNotice::getStatus, 0)
                        .le(ScheduledNotice::getScheduledAt, LocalDateTime.now())
        );
        if (pending.isEmpty()) return;

        for (ScheduledNotice notice : pending) {
            try {
                SendNotificationRequest req = new SendNotificationRequest();
                req.setTitle(notice.getTitle());
                req.setContent(notice.getContent());
                req.setNotifyType(notice.getNotifyType());
                if ("single".equals(notice.getTargetType()) && notice.getTargetValue() != null) {
                    req.setUserId(Long.parseLong(notice.getTargetValue()));
                } else if ("role".equals(notice.getTargetType())) {
                    req.setRole(notice.getTargetValue());
                }
                notificationService.sendNotification(req);
                notice.setStatus(1);
                log.info("定时公告已发送: id={}, title={}", notice.getId(), notice.getTitle());
            } catch (Exception e) {
                notice.setStatus(2);
                log.error("定时公告发送失败: id={}", notice.getId(), e);
            }
            scheduledNoticeMapper.updateById(notice);
        }
    }
}
