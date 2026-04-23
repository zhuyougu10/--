package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.AdminLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台认证API", description = "后台管理端认证接口")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AuthService authService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(authService.adminLogin(request));
    }

    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/profile")
    public Result<LoginResponse> getProfile() {
        Long adminId = UserContext.getCurrentUserId();
        return Result.success(authService.getAdminProfile(adminId));
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
