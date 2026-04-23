package com.stadium.booking.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserResponse {
    private Long id;
    private String username;
    private String name;
    private Integer status;
    private List<String> roleCodes;
    private List<String> roleTexts;
    private List<Long> venueIds;
    private List<String> venueNames;
}
