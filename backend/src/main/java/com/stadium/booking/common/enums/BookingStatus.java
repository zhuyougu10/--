package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    CONFIRMED(1, "已确认"),
    CANCELLED(2, "已取消"),
    CHECKED_IN(3, "已签到"),
    NO_SHOW(4, "爽约");

    private final Integer code;
    private final String desc;

    public static BookingStatus fromCode(Integer code) {
        for (BookingStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
