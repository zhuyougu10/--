package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.MarkNoShowRequest;
import com.stadium.booking.dto.response.ViolationResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "违约管理", description = "后台违约管理接口")
@RestController
@RequestMapping("/admin/violations")
@RequiredArgsConstructor
public class ViolationAdminController {
    private final ViolationService violationService;

    @Operation(summary = "标记爽约")
    @PostMapping("/no-show")
    @RequirePermission("booking:mark_no_show")
    public Result<ViolationResponse> markNoShow(@Valid @RequestBody MarkNoShowRequest request) {
        return Result.success(violationService.markNoShow(request));
    }

    @Operation(summary = "清除违约记录")
    @DeleteMapping("/{id}")
    @RequirePermission("user:update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> clearViolation(@PathVariable Long id) {
        violationService.clearViolation(id);
        return Result.success();
    }

    @Operation(summary = "查询用户违约记录")
    @GetMapping("/user/{userId}")
    @RequirePermission("user:read")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ViolationResponse>> getUserViolations(@PathVariable Long userId) {
        return Result.success(violationService.getUserViolations(userId));
    }

    @Operation(summary = "获取违约记录详情")
    @GetMapping("/{id}")
    @RequirePermission("user:read")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ViolationResponse> getViolationById(@PathVariable Long id) {
        return Result.success(violationService.getViolationById(id));
    }
}
