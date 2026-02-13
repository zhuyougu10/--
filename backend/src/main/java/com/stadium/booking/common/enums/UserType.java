package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum UserType {
    STUDENT(1, "学生"),
    TEACHER(2, "教师"),
    EXTERNAL(3, "外部人员");

    private final Integer code;
    private final String desc;
}
