package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("booking")
public class Booking extends BaseEntity {
    private String bookingNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long venueId;
    private String venueName;
    private Long courtId;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotCount;
    private Integer bookingType;
    private Long groupId;
    private Integer status;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private Integer cancelledByType;
    private LocalDateTime checkedInAt;
    private Long checkedInBy;
    private Integer checkinMethod;
    private LocalDateTime noShowMarkedAt;
    private Long noShowMarkedBy;
    private String remark;
}
