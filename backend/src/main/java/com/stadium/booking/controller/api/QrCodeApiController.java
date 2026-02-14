package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.QrCodeResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "二维码API", description = "小程序端二维码接口")
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeApiController {
    private final CheckinService checkinService;

    @Operation(summary = "获取核销二维码")
    @GetMapping("/booking/{bookingNo}")
    public Result<QrCodeResponse> getQrCode(@PathVariable String bookingNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(checkinService.generateQrToken(userId, bookingNo));
    }
}
