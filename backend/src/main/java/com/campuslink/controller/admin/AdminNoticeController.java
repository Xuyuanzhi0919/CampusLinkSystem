package com.campuslink.controller.admin;

import com.campuslink.common.Result;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.entity.Notification;
import com.campuslink.service.AdminService;
import com.campuslink.service.NotificationService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员公告/通知接口
 * AdminAuthInterceptor 已确保只有 admin 可访问
 */
@Tag(name = "管理端 - 公告管理")
@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NotificationService notificationService;
    private final AdminService adminService;

    @Operation(summary = "发送系统公告", description = "userId 为空时广播给全部用户，指定 userId 则发送给单个用户")
    @PostMapping("/send")
    public Result<Void> send(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request);
        return Result.success(request.getUserId() == null ? "公告已广播给全体用户" : "通知已发送");
    }

    @Operation(summary = "公告发送历史", description = "返回最近20条广播记录（去重）")
    @GetMapping("/history")
    public Result<List<Notification>> getHistory() {
        return Result.success(adminService.getNoticeHistory());
    }
}
