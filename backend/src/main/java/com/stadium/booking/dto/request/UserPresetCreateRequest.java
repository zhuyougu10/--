package com.stadium.booking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPresetCreateRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @NotBlank(message = "工号/学号不能为空")
    @Size(max = 50, message = "工号/学号长度不能超过50")
    private String studentNo;

    @NotNull(message = "用户类型不能为空")
    @Min(value = 1, message = "用户类型非法")
    @Max(value = 3, message = "用户类型非法")
    private Integer userType;
}
