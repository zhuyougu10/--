package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourtResponse {
    private Long id;
    private Long venueId;
    private String venueName;
    private String name;
    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer status;
    private String statusReason;
    private LocalDateTime statusUntil;
    private Integer sortOrder;
}
