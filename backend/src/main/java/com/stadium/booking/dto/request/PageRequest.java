package com.stadium.booking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;

@Data
public class PageRequest {
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer size = 10;
}
