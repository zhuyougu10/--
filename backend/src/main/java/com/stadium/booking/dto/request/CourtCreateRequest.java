package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtCreateRequest {
    @NotNull(message = "球馆ID不能为空")
    private Long venueId;

    @NotBlank(message = "场地名称不能为空")
    private String name;

    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer sortOrder = 0;
}
