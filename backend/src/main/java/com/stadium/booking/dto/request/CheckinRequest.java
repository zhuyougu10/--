package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckinRequest {
    @NotBlank(message = "token不能为空")
    private String token;

    private Integer checkinMethod = 1;
}
