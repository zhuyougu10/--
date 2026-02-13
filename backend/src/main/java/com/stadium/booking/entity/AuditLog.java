package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_log")
public class AuditLog extends BaseEntity {
    private Long userId;
    private Integer userType;
    private String username;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
}
