package com.stadium.booking.service;

import cn.hutool.json.JSONUtil;
import com.stadium.booking.entity.AuditLog;
import com.stadium.booking.repository.AuditLogRepository;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(String action, String resourceType, Long resourceId, String resourceName, Object oldValue, Object newValue) {
        AuditLog auditLog = new AuditLog();
        
        UserPrincipal currentUser = UserContext.getCurrentUser();
        if (currentUser != null) {
            auditLog.setUserId(currentUser.getUserId());
            auditLog.setUserType(getUserTypeCode(currentUser.getUserType()));
            auditLog.setUsername(currentUser.getUsername());
        }

        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setResourceName(resourceName);
        
        if (oldValue != null) {
            auditLog.setOldValue(JSONUtil.toJsonStr(oldValue));
        }
        if (newValue != null) {
            auditLog.setNewValue(JSONUtil.toJsonStr(newValue));
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }

        auditLogRepository.insert(auditLog);
        log.info("Audit: {} {} {} ({})", action, resourceType, resourceId, resourceName);
    }

    private Integer getUserTypeCode(String userType) {
        if (userType == null) return 1;
        return switch (userType.toLowerCase()) {
            case "admin" -> 3;
            case "staff" -> 2;
            default -> 1;
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
