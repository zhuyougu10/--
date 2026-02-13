package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingResponse {
    private Long id;
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
    private Integer status;
    private String statusText;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
    private String remark;
    private LocalDateTime createdAt;
    private String qrToken;
}
