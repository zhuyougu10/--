package com.stadium.booking.security;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.RolePermissionRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAspectTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private AdminRoleRepository adminRoleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onlyAdminRoleCanBypassPermissionCheck() throws Throwable {
        PermissionAspect aspect = new PermissionAspect(adminUserRepository, adminRoleRepository, rolePermissionRepository);
        setAuthentication(new UserPrincipal(1L, "ADMIN", true, "admin"));
        when(adminRoleRepository.findRoleCodesByAdminUserId(1L)).thenReturn(java.util.List.of("ADMIN"));
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.checkPermission(joinPoint, permission("venue:read"));

        assertEquals("ok", result);
        verify(rolePermissionRepository, never()).hasPermission(1L, "venue:read");
    }

    @Test
    void venueStaffMustStillPassPermissionCheckEvenIfAdminFlagIsTrue() {
        PermissionAspect aspect = new PermissionAspect(adminUserRepository, adminRoleRepository, rolePermissionRepository);
        setAuthentication(new UserPrincipal(2L, "VENUE_STAFF", true, "staff"));

        AdminUser admin = new AdminUser();
        admin.setId(2L);
        when(adminRoleRepository.findRoleCodesByAdminUserId(2L)).thenReturn(java.util.List.of("VENUE_STAFF"));
        when(adminUserRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(rolePermissionRepository.hasPermission(2L, "venue:read")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> aspect.checkPermission(joinPoint, permission("venue:read")));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(rolePermissionRepository).hasPermission(2L, "venue:read");
    }

    private void setAuthentication(UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }

    private RequirePermission permission(String code) {
        try {
            Method method = SecuredMethods.class.getDeclaredMethod("securedMethod");
            RequirePermission annotation = method.getAnnotation(RequirePermission.class);
            return new RequirePermission() {
                @Override
                public String value() {
                    return code != null ? code : annotation.value();
                }

                @Override
                public Class<? extends java.lang.annotation.Annotation> annotationType() {
                    return RequirePermission.class;
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class SecuredMethods {
        @RequirePermission("venue:read")
        public void securedMethod() {
        }
    }
}
