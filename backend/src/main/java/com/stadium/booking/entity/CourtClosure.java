package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("court_closure")
public class CourtClosure extends BaseEntity {
    private Long courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Long createdBy;
}
