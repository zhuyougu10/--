package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.UserProfileUpdateRequest;
import com.stadium.booking.dto.request.BindStudentNoRequest;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户API", description = "小程序端用户接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(userService.getUserDetail(userId));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(userService.updateProfile(userId, request));
    }

    @Operation(summary = "绑定工号/学号")
    @PostMapping("/bind")
    public Result<UserResponse> bindStudentNo(@Valid @RequestBody BindStudentNoRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(userService.bindStudentNo(userId, request));
    }
}
