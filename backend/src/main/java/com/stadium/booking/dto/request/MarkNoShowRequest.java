package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkNoShowRequest {
    @NotBlank(message = "预约单号不能为空")
    private String bookingNo;
}
