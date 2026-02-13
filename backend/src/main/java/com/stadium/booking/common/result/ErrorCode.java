package com.stadium.booking.common.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SLOT_CONFLICT(409, "SLOT_CONFLICT", "时段已被占用"),
    OUT_OF_OPEN_HOURS(400, "OUT_OF_OPEN_HOURS", "不在营业时间"),
    OUT_OF_BOOKING_WINDOW(400, "OUT_OF_BOOKING_WINDOW", "超出可预约窗口"),
    LIMIT_EXCEEDED(403, "LIMIT_EXCEEDED", "超过个人限额"),
    CANCEL_NOT_ALLOWED(403, "CANCEL_NOT_ALLOWED", "已超过取消截止时间"),
    CHECKIN_NOT_ALLOWED(403, "CHECKIN_NOT_ALLOWED", "不在核销窗口"),
    INVALID_REQUEST(400, "INVALID_REQUEST", "参数非法"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "未登录"),
    FORBIDDEN(403, "FORBIDDEN", "无权限"),
    NOT_FOUND(404, "NOT_FOUND", "资源不存在"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "服务内部错误"),
    USER_BANNED(403, "USER_BANNED", "账号已被禁用");

    private final Integer httpStatus;
    private final String code;
    private final String message;
}
