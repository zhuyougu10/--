package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CheckinResponse {
    private Boolean success;
    private String message;
    private String bookingNo;
    private String venueName;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String userName;
    private LocalDateTime checkedInAt;
}
