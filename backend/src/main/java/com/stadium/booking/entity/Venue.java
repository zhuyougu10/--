package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("venue")
public class Venue extends BaseEntity {
    private Long campusId;
    private String name;
    private String code;
    private String sportType;
    private String location;
    private String description;
    private String imageUrl;
    private String openDays;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotMinutes;
    private Integer bookAheadDays;
    private Integer cancelCutoffMinutes;
    private Integer checkinWindowBefore;
    private Integer noShowGraceMinutes;
    private Integer dailySlotLimit;
    private Integer weeklySlotLimit;
    private Integer groupBookingEnabled;
    private Integer groupMaxCourts;
    private Integer groupMaxHours;
    private Integer status;
}
