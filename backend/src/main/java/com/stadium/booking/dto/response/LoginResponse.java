package com.stadium.booking.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String userType;
    private Boolean isNewUser;
    private Boolean isBound;
    private Boolean needBind;
}
