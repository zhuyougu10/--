package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingCreateRequest {
    @NotNull(message = "球馆ID不能为空")
    private Long venueId;

    @NotNull(message = "场地ID不能为空")
    private Long courtId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    private String remark;

    private Integer bookingType = 1;
}
