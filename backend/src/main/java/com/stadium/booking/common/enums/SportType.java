package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum SportType {
    BADMINTON("badminton", "羽毛球"),
    BASKETBALL("basketball", "篮球"),
    TABLE_TENNIS("table_tennis", "乒乓球"),
    TENNIS("tennis", "网球"),
    VOLLEYBALL("volleyball", "排球"),
    FOOTBALL("football", "足球");

    private final String code;
    private final String desc;
}
