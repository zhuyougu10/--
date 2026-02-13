package com.stadium.booking.security;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final AdminUserRepository adminUserRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        if (principal.getIsAdmin()) {
            return joinPoint.proceed();
        }

        AdminUser admin = adminUserRepository.findById(principal.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        boolean hasPermission = rolePermissionRepository.hasPermission(admin.getId(), requirePermission.value());
        
        if (!hasPermission) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return joinPoint.proceed();
    }
}
