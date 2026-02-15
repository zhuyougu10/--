package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindStudentNoRequest {
    @NotBlank(message = "工号/学号不能为空")
    @Size(max = 50, message = "工号/学号长度不能超过50")
    private String studentNo;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;
}
