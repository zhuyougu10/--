package com.stadium.booking.service;

import cn.hutool.crypto.digest.BCrypt;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.AdminLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import com.stadium.booking.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private WechatService wechatService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private AdminRoleRepository adminRoleRepository;
    @Mock
    private VenueStaffRepository venueStaffRepository;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void adminLoginSignsVenueStaffRole() {
        AdminUser admin = buildAdminUser();
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("staff1");
        request.setPassword("secret123");

        when(adminUserRepository.findByUsername("staff1")).thenReturn(Optional.of(admin));
        when(adminRoleRepository.findRoleCodesByAdminUserId(1L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.findVenueIdsByAdminUserId(1L)).thenReturn(List.of(10L));
        when(jwtUtils.generateToken(1L, "VENUE_STAFF", false)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(1L)).thenReturn("refresh-token");

        LoginResponse response = authService.adminLogin(request);

        assertEquals("VENUE_STAFF", response.getRole());
        assertEquals("场馆管理员", response.getRoleText());
        verify(jwtUtils).generateToken(1L, "VENUE_STAFF", false);
    }

    @Test
    void adminLoginPrefersAdminRoleWhenMultipleRolesExist() {
        AdminUser admin = buildAdminUser();
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("staff1");
        request.setPassword("secret123");

        when(adminUserRepository.findByUsername("staff1")).thenReturn(Optional.of(admin));
        when(adminRoleRepository.findRoleCodesByAdminUserId(1L)).thenReturn(List.of("VENUE_STAFF", "ADMIN"));
        when(jwtUtils.generateToken(1L, "ADMIN", true)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(1L)).thenReturn("refresh-token");

        LoginResponse response = authService.adminLogin(request);

        assertEquals("ADMIN", response.getRole());
        assertEquals("管理员", response.getRoleText());
        verify(jwtUtils).generateToken(1L, "ADMIN", true);
    }

    @Test
    void adminLoginRejectsAccountWithoutBackendRole() {
        AdminUser admin = buildAdminUser();
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("staff1");
        request.setPassword("secret123");

        when(adminUserRepository.findByUsername("staff1")).thenReturn(Optional.of(admin));
        when(adminRoleRepository.findRoleCodesByAdminUserId(1L)).thenReturn(List.of("USER"));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.adminLogin(request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    void adminLoginRejectsVenueStaffWithoutAssignedVenues() {
        AdminUser admin = buildAdminUser();
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("staff1");
        request.setPassword("secret123");

        when(adminUserRepository.findByUsername("staff1")).thenReturn(Optional.of(admin));
        when(adminRoleRepository.findRoleCodesByAdminUserId(1L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.findVenueIdsByAdminUserId(1L)).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.adminLogin(request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    private AdminUser buildAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setId(1L);
        admin.setUsername("staff1");
        admin.setName("测试管理员");
        admin.setStatus(1);
        admin.setPasswordHash(BCrypt.hashpw("secret123"));
        return admin;
    }
}
