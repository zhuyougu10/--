package com.stadium.booking.service;

import cn.hutool.crypto.digest.BCrypt;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.AdminUserCreateRequest;
import com.stadium.booking.dto.request.AdminUserVenueAssignRequest;
import com.stadium.booking.dto.response.AdminUserResponse;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {
    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final VenueRepository venueRepository;
    private final VenueStaffRepository venueStaffRepository;

    public List<AdminUserResponse> listAll() {
        List<AdminUser> adminUsers = adminUserRepository.findAll();
        return adminUsers.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminUserResponse createAdminUser(AdminUserCreateRequest request) {
        String username = normalizeRequired(request.getUsername(), "用户名不能为空");
        String name = normalizeRequired(request.getName(), "姓名不能为空");
        String password = normalizeRequired(request.getPassword(), "初始密码不能为空");
        List<Long> venueIds = request.getVenueIds() == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(request.getVenueIds()));

        if (venueIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "至少分配一个球馆");
        }
        if (adminUserRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户名已存在");
        }

        List<Long> existingVenueIds = venueRepository.findByIds(venueIds).stream().map(Venue::getId).toList();
        if (existingVenueIds.size() != venueIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "存在无效球馆，无法创建账号");
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setName(name);
        adminUser.setPhone(normalizeOptional(request.getPhone()));
        adminUser.setEmail(normalizeOptional(request.getEmail()));
        adminUser.setPasswordHash(BCrypt.hashpw(password));
        adminUser.setStatus(1);
        try {
            adminUserRepository.insert(adminUser);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户名已存在");
        }

        adminRoleRepository.addRoleToAdminUser(adminUser.getId(), "VENUE_STAFF");
        for (Long venueId : venueIds) {
            venueStaffRepository.insertBinding(adminUser.getId(), venueId);
        }
        return toResponse(adminUser);
    }

    @Transactional
    public AdminUserResponse updateManagedVenues(Long adminUserId, AdminUserVenueAssignRequest request) {
        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "后台账号不存在"));

        if (adminRoleRepository.hasRole(adminUserId, "ADMIN")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "管理员账号拥有全局权限，不能配置受限球馆");
        }

        List<Long> venueIds = request.getVenueIds() == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(request.getVenueIds()));
        if (!venueIds.isEmpty()) {
            List<Long> existingVenueIds = venueRepository.findByIds(venueIds).stream().map(Venue::getId).toList();
            if (existingVenueIds.size() != venueIds.size()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "存在无效球馆，无法保存关联关系");
            }
            if (!adminRoleRepository.hasRole(adminUserId, "VENUE_STAFF")) {
                adminRoleRepository.addRoleToAdminUser(adminUserId, "VENUE_STAFF");
            }
        }

        venueStaffRepository.deleteByAdminUserId(adminUserId);
        for (Long venueId : venueIds) {
            venueStaffRepository.insertBinding(adminUserId, venueId);
        }
        if (venueIds.isEmpty()) {
            adminRoleRepository.removeRoleFromAdminUser(adminUserId, "VENUE_STAFF");
        }

        return toResponse(adminUser);
    }

    private AdminUserResponse toResponse(AdminUser adminUser) {
        List<String> roleCodes = adminRoleRepository.findRoleCodesByAdminUserId(adminUser.getId());
        List<Long> venueIds = venueStaffRepository.findVenueIdsByAdminUserId(adminUser.getId());
        Map<Long, String> venueNames = venueIds.isEmpty()
                ? Map.of()
                : venueRepository.findByIds(venueIds).stream().collect(Collectors.toMap(Venue::getId, Venue::getName));

        AdminUserResponse response = new AdminUserResponse();
        response.setId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setName(adminUser.getName());
        response.setStatus(adminUser.getStatus());
        response.setRoleCodes(roleCodes);
        response.setRoleTexts(roleCodes.stream().map(this::mapRoleText).toList());
        response.setVenueIds(venueIds);
        response.setVenueNames(venueIds.stream().map(id -> venueNames.getOrDefault(id, String.valueOf(id))).toList());
        return response;
    }

    private String mapRoleText(String roleCode) {
        return switch (roleCode) {
            case "ADMIN" -> "管理员";
            case "VENUE_STAFF" -> "场馆管理员";
            default -> roleCode;
        };
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
