package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class AdminUser extends BaseEntity {
    private String username;
    private String passwordHash;
    private String name;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
