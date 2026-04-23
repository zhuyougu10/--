package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import com.stadium.booking.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVenueAccessServiceTest {

    @Mock
    private AdminRoleRepository adminRoleRepository;

    @Mock
    private VenueStaffRepository venueStaffRepository;

    @InjectMocks
    private AdminVenueAccessService adminVenueAccessService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void venueStaffGetsOnlyAssignedVenueIds() {
        setAuthentication(new UserPrincipal(2L, "VENUE_STAFF", false, "staff"));
        when(adminRoleRepository.findRoleCodesByAdminUserId(2L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.findVenueIdsByAdminUserId(2L)).thenReturn(List.of(10L, 11L));

        List<Long> venueIds = adminVenueAccessService.getCurrentManagedVenueIds();

        assertEquals(List.of(10L, 11L), venueIds);
    }

    @Test
    void venueStaffWithoutAssignmentsGetsEmptyVenueIds() {
        setAuthentication(new UserPrincipal(2L, "VENUE_STAFF", false, "staff"));
        when(adminRoleRepository.findRoleCodesByAdminUserId(2L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.findVenueIdsByAdminUserId(2L)).thenReturn(List.of());

        List<Long> venueIds = adminVenueAccessService.getCurrentManagedVenueIds();

        assertEquals(List.of(), venueIds);
    }

    @Test
    void venueStaffCannotAccessUnassignedVenue() {
        setAuthentication(new UserPrincipal(2L, "VENUE_STAFF", false, "staff"));
        when(adminRoleRepository.findRoleCodesByAdminUserId(2L)).thenReturn(List.of("VENUE_STAFF"));
        when(venueStaffRepository.existsByAdminUserIdAndVenueId(2L, 99L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminVenueAccessService.checkVenueAccess(99L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    private void setAuthentication(UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
