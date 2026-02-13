package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotResponse {
    private Long courtId;
    private String courtName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private BookingInfo booking;

    @Data
    public static class BookingInfo {
        private String bookingNo;
        private String userName;
        private String userPhone;
    }
}
