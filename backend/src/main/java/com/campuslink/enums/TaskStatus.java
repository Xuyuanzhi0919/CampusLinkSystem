package com.campuslink.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatus {
    /**
     * 待接单 - 任务已发布,等待接取
     */
    ACTIVE(0, "待接单", "任务已发布,等待接取"),

    /**
     * 已接取 - 已有人接取,准备开始执行
     */
    ACCEPTED(1, "已接取", "任务已被接取"),

    /**
     * 进行中 - 正在执行任务
     */
    IN_PROGRESS(2, "进行中", "任务正在执行中"),

    /**
     * 待确认 - 执行者已提交结果,等待发布者确认
     */
    SUBMITTED(3, "待确认", "执行者已提交结果,等待发布者确认"),

    /**
     * 已完成 - 发布者确认完成,积分已发放
     */
    COMPLETED(4, "已完成", "任务已完成,积分已发放"),

    /**
     * 已取消 - 任务被取消
     */
    CANCELLED(5, "已取消", "任务已取消"),

    /**
     * 已超时 - 超过截止时间未完成
     */
    EXPIRED(6, "已超时", "任务已超过截止时间");

    private final Integer code;
    private final String name;
    private final String description;

    TaskStatus(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static TaskStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查状态转换是否合法
     */
    public boolean canTransitionTo(TaskStatus target) {
        if (target == null) {
            return false;
        }

        // 定义合法的状态转换
        switch (this) {
            case ACTIVE:
                // 待接单 -> 已接取、已取消、已超时
                return target == ACCEPTED || target == CANCELLED || target == EXPIRED;

            case ACCEPTED:
                // 已接取 -> 进行中、已取消、已超时
                return target == IN_PROGRESS || target == CANCELLED || target == EXPIRED;

            case IN_PROGRESS:
                // 进行中 -> 待确认、已取消、已超时
                return target == SUBMITTED || target == CANCELLED || target == EXPIRED;

            case SUBMITTED:
                // 待确认 -> 已完成、进行中(拒绝后重新执行)、已超时
                return target == COMPLETED || target == IN_PROGRESS || target == EXPIRED;

            case COMPLETED:
            case CANCELLED:
            case EXPIRED:
                // 终态,不能再转换
                return false;

            default:
                return false;
        }
    }

    /**
     * 是否是终态(不可再转换的状态)
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED || this == EXPIRED;
    }

    /**
     * 是否是活动状态(可以继续操作的状态)
     */
    public boolean isActiveState() {
        return this == ACTIVE || this == ACCEPTED || this == IN_PROGRESS || this == SUBMITTED;
    }
}
