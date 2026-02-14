package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {
    private Long id;
    private Long bookingId;
    private String bookingNo;
    private String qrToken;
    private Integer checkinMethod;
    private Long checkedInBy;
    private LocalDateTime checkedInAt;
    private Long venueId;
    private LocalDateTime createdAt;
}
