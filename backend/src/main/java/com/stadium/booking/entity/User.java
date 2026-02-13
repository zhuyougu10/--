package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String openid;
    private String unionId;
    private String name;
    private String phone;
    private String studentNo;
    private Integer userType;
    private String avatar;
    private Integer status;
    private LocalDateTime bannedUntil;
    private Integer noShowCount;
    private LocalDateTime lastNoShowAt;
}
