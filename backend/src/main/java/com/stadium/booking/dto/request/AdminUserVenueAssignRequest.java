package com.stadium.booking.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserVenueAssignRequest {
    private List<Long> venueIds;
}
