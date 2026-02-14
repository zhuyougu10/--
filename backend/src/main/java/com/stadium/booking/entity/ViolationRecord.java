package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation_record")
public class ViolationRecord extends BaseEntity {
    private Long userId;
    private Long bookingId;
    private Integer violationType;
    private LocalDate bookingDate;
    private Long markedBy;
    private LocalDateTime markedAt;
    private Integer banDays;
    private LocalDateTime banUntil;
    private LocalDateTime clearedAt;
    private Long clearedBy;
}
