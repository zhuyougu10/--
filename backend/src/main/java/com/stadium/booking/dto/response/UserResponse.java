package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private Integer userType;
    private String userTypeText;
    private Integer status;
    private String statusText;
    private Integer noShowCount;
    private LocalDateTime lastNoShowAt;
    private LocalDateTime bannedUntil;
    private LocalDateTime createdAt;
}
