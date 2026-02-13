package com.stadium.booking.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String userType;
    private Boolean isAdmin;
}
