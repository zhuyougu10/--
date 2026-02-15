package com.stadium.booking.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;
    
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
    
    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatar;
}
