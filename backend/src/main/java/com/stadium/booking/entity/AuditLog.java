package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
