package com.stadium.booking.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RecommendationRequest {
    private Long venueId;
    private String sportType;
    
    private LocalDate date;
    private LocalTime preferredStartTime;
    private Integer durationMinutes = 60;
    
    private Integer maxResults = 10;
    private Integer timeOffsetSlots = 2;
    private Boolean allowAlternativeVenue = true;
}
