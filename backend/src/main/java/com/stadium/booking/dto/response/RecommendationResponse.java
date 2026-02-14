package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class RecommendationResponse {
    private List<RecommendationItem> recommendations;
    private Integer totalCount;

    @Data
    public static class RecommendationItem {
        private Long venueId;
        private String venueName;
        private Long courtId;
        private String courtName;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String label;
        private String reason;
        private Integer score;
    }
}
