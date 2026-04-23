package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.AdminUserCreateRequest;
import com.stadium.booking.dto.response.AdminUserResponse;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private AdminRoleRepository adminRoleRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private VenueStaffRepository venueStaffRepository;

    @InjectMocks
    private AdminUserManagementService adminUserManagementService;

    @Test
    void createAdminUserCreatesVenueStaffWithManagedVenues() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("venue_staff_01");
        request.setName("场馆管理员甲");
        request.setPhone("13800000001");
        request.setEmail("staff01@example.com");
        request.setPassword("secret123");
        request.setVenueIds(List.of(11L, 12L));

        when(adminUserRepository.findByUsername("venue_staff_01")).thenReturn(Optional.empty());
        when(venueRepository.findByIds(List.of(11L, 12L))).thenReturn(List.of(buildVenue(11L, "东馆"), buildVenue(12L, "西馆")));
        when(adminUserRepository.insert(any(AdminUser.class))).thenAnswer(invocation -> {
            AdminUser adminUser = invocation.getArgument(0);
            adminUser.setId(9L);
            return 1;
        });
        when(adminRoleRepository.findRoleCodesByAdminUserId(9L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.findVenueIdsByAdminUserId(9L)).thenReturn(List.of(11L, 12L));

        AdminUserResponse response = adminUserManagementService.createAdminUser(request);

        ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserRepository).insert(captor.capture());
        AdminUser saved = captor.getValue();
        assertEquals("venue_staff_01", saved.getUsername());
        assertEquals("场馆管理员甲", saved.getName());
        assertEquals(1, saved.getStatus());
        assertNotNull(saved.getPasswordHash());
        assertFalse(saved.getPasswordHash().contains("secret123"));
        verify(adminRoleRepository).addRoleToAdminUser(9L, "VENUE_STAFF");
        verify(venueStaffRepository).insertBinding(9L, 11L);
        verify(venueStaffRepository).insertBinding(9L, 12L);
        assertEquals(List.of("场馆管理员"), response.getRoleTexts());
        assertEquals(List.of("东馆", "西馆"), response.getVenueNames());
    }

    @Test
    void createAdminUserRejectsEmptyVenues() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("venue_staff_01");
        request.setName("场馆管理员甲");
        request.setPassword("secret123");
        request.setVenueIds(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminUserManagementService.createAdminUser(request));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    private Venue buildVenue(Long id, String name) {
        Venue venue = new Venue();
        venue.setId(id);
        venue.setName(name);
        return venue;
    }
}
