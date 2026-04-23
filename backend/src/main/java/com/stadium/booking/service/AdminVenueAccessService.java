package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVenueAccessService {
    private final AdminRoleRepository adminRoleRepository;
    private final VenueStaffRepository venueStaffRepository;

    public boolean isCurrentAdminRole() {
        return "ADMIN".equals(resolveCurrentRole());
    }

    public boolean isCurrentVenueStaffRole() {
        return "VENUE_STAFF".equals(resolveCurrentRole());
    }

    public List<Long> getCurrentManagedVenueIds() {
        if (!isCurrentVenueStaffRole()) {
            return List.of();
        }
        Long adminUserId = UserContext.getCurrentUserId();
        if (adminUserId == null) {
            return List.of();
        }
        return venueStaffRepository.findVenueIdsByAdminUserId(adminUserId);
    }

    public void checkVenueAccess(Long venueId) {
        String role = resolveCurrentRole();
        if ("ADMIN".equals(role)) {
            return;
        }
        if (!"VENUE_STAFF".equals(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号无权访问后台球馆数据");
        }
        Long adminUserId = UserContext.getCurrentUserId();
        if (adminUserId == null || venueId == null || !venueStaffRepository.existsByAdminUserIdAndVenueId(adminUserId, venueId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该球馆数据");
        }
    }

    private String resolveCurrentRole() {
        String currentType = UserContext.getCurrentUserType();
        if (!"ADMIN".equals(currentType) && !"VENUE_STAFF".equals(currentType)) {
            return currentType;
        }
        Long adminUserId = UserContext.getCurrentUserId();
        if (adminUserId == null) {
            return null;
        }
        List<String> roleCodes = adminRoleRepository.findRoleCodesByAdminUserId(adminUserId);
        if (roleCodes.contains("ADMIN")) {
            return "ADMIN";
        }
        if (roleCodes.contains("VENUE_STAFF")) {
            return "VENUE_STAFF";
        }
        return null;
    }
}
