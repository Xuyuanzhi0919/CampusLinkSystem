package com.campuslink.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    NO_CONTENT(204, "删除成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误码 10000+
    // 认证用户 10001-10008
    USER_NOT_FOUND(10001, "用户不存在"),
    PASSWORD_ERROR(10002, "密码错误"),
    USER_DISABLED(10003, "用户已被禁用"),
    TOKEN_EXPIRED(10004, "Token已过期"),
    TOKEN_INVALID(10005, "Token无效"),
    VERIFY_CODE_ERROR(10006, "验证码错误"),
    VERIFY_CODE_EXPIRED(10007, "验证码已过期"),
    USER_ALREADY_EXISTS(10008, "用户已存在"),

    // 资源文件 20001-20005
    RESOURCE_NOT_FOUND(20001, "资源不存在"),
    RESOURCE_UPLOAD_FAILED(20002, "资源上传失败"),
    RESOURCE_DOWNLOAD_FAILED(20003, "资源下载失败"),
    INSUFFICIENT_POINTS(20004, "积分不足"),
    RESOURCE_NOT_APPROVED(20005, "资源未审核通过"),

    // 问答模块 30001-30004
    QUESTION_NOT_FOUND(30001, "问题不存在"),
    ANSWER_NOT_FOUND(30002, "回答不存在"),
    ANSWER_ALREADY_ACCEPTED(30003, "已采纳其他回答"),
    QUESTION_ALREADY_SOLVED(30004, "问题已解决"),
    PERMISSION_DENIED(30005, "无权执行此操作"),

    // 任务模块 40001-40004
    TASK_NOT_FOUND(40001, "任务不存在"),
    TASK_ALREADY_ACCEPTED(40002, "任务已被接单"),
    CANNOT_ACCEPT_OWN_TASK(40003, "不能接受自己发布的任务"),
    TASK_NOT_IN_PROGRESS(40004, "任务未在进行中"),

    // 活动社团 50001-50010
    ACTIVITY_NOT_FOUND(50001, "活动不存在"),
    ACTIVITY_FULL(50002, "活动已满员"),
    ALREADY_REGISTERED(50003, "已报名该活动"),
    CLUB_NOT_FOUND(50004, "社团不存在"),
    CLUB_DISBANDED(50005, "社团已解散"),
    ALREADY_JOINED_CLUB(50006, "已加入该社团"),
    NOT_CLUB_MEMBER(50007, "不是社团成员"),
    FOUNDER_CANNOT_LEAVE(50008, "创始人不能退出社团"),
    NOT_CLUB_ADMIN(50009, "不是社团管理员"),
    NOT_ACTIVITY_PARTICIPANT(50010, "未报名该活动");

    private final Integer code;
    private final String message;
}
