package com.stadium.booking.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String name;
    private Integer userType;
    private String userTypeText;
    private Boolean isNewUser;
    private Boolean isBound;
    private Boolean needBind;
}
