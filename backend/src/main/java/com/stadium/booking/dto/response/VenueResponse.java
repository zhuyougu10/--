package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class VenueResponse {
    private Long id;
    private String name;
    private String code;
    private String sportType;
    private String location;
    private String description;
    private String imageUrl;
    private String openDays;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotMinutes;
    private Integer bookAheadDays;
    private Integer cancelCutoffMinutes;
    private Integer checkinWindowBefore;
    private Integer noShowGraceMinutes;
    private Integer dailySlotLimit;
    private Integer weeklySlotLimit;
    private Integer status;
    private Integer courtCount;
    private List<CourtResponse> courts;
}
