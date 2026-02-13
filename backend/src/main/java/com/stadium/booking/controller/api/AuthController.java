package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.WechatLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证API", description = "小程序端认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "微信登录")
    @PostMapping("/wechat/login")
    public Result<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return Result.success(authService.wechatLogin(request));
    }
}
