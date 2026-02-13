package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class VenueCreateRequest {
    @NotBlank(message = "球馆名称不能为空")
    private String name;

    @NotBlank(message = "球馆编码不能为空")
    private String code;

    @NotBlank(message = "运动类型不能为空")
    private String sportType;

    private String location;
    private String description;
    private String imageUrl;

    private String openDays = "1,2,3,4,5,6,7";

    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;

    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;

    private Integer slotMinutes = 60;
    private Integer bookAheadDays = 7;
    private Integer cancelCutoffMinutes = 30;
    private Integer checkinWindowBefore = 15;
    private Integer noShowGraceMinutes = 15;
    private Integer dailySlotLimit = 2;
    private Integer weeklySlotLimit = 10;
    private Integer groupBookingEnabled = 1;
    private Integer groupMaxCourts = 4;
    private Integer groupMaxHours = 4;
}
