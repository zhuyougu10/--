package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ViolationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookingId;
    private String bookingNo;
    private Integer violationType;
    private String violationTypeText;
    private LocalDate bookingDate;
    private LocalDateTime markedAt;
    private Integer banDays;
    private LocalDateTime banUntil;
    private LocalDateTime clearedAt;
}
