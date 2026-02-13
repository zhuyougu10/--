package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("court")
public class Court extends BaseEntity {
    private Long venueId;
    private String name;
    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer status;
    private String statusReason;
    private LocalDateTime statusUntil;
    private Integer sortOrder;
}
