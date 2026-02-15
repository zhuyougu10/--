package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.dto.response.ViolationResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.BookingService;
import com.stadium.booking.service.ViolationService;
import com.stadium.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "用户管理", description = "后台用户管理接口")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;
    private final BookingService bookingService;
    private final ViolationService violationService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    @RequirePermission("user:read")
    public Result<IPage<UserResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.listPage(current, size, keyword, status));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @RequirePermission("user:read")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    @Operation(summary = "获取用户预约记录")
    @GetMapping("/{id}/bookings")
    @RequirePermission("user:read")
    public Result<List<BookingResponse>> getUserBookings(@PathVariable Long id) {
        return Result.success(bookingService.getUserBookings(id, null));
    }

    @Operation(summary = "获取用户违约记录")
    @GetMapping("/{id}/violations")
    @RequirePermission("user:read")
    public Result<List<ViolationResponse>> getUserViolations(@PathVariable Long id) {
        return Result.success(violationService.getUserViolations(id));
    }

    @Operation(summary = "更新用户状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("user:update")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}
