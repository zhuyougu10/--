package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.AdminUserVenueAssignRequest;
import com.stadium.booking.dto.response.AdminUserResponse;
import com.stadium.booking.service.AdminUserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "后台账号管理", description = "后台账号与场馆分配接口")
@RestController
@RequestMapping("/admin/admin-users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserAdminController {
    private final AdminUserManagementService adminUserManagementService;

    @Operation(summary = "查询后台账号列表")
    @GetMapping
    public Result<List<AdminUserResponse>> list() {
        return Result.success(adminUserManagementService.listAll());
    }

    @Operation(summary = "更新后台账号可管理球馆")
    @PutMapping("/{id}/venues")
    public Result<AdminUserResponse> updateManagedVenues(
            @PathVariable Long id,
            @RequestBody AdminUserVenueAssignRequest request) {
        return Result.success(adminUserManagementService.updateManagedVenues(id, request));
    }
}
