package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.CheckinRequest;
import com.stadium.booking.dto.request.ManualCheckinRequest;
import com.stadium.booking.dto.response.CheckinResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "核销管理", description = "后台核销接口")
@RestController
@RequestMapping("/admin/checkin")
@RequiredArgsConstructor
public class CheckinAdminController {
    private final CheckinService checkinService;

    @Operation(summary = "扫码核销")
    @PostMapping("/scan")
    @RequirePermission("booking:checkin")
    public Result<CheckinResponse> scanCheckin(@Valid @RequestBody CheckinRequest request) {
        return Result.success(checkinService.checkin(request));
    }

    @Operation(summary = "手动核销")
    @PostMapping("/manual")
    @RequirePermission("booking:checkin")
    public Result<CheckinResponse> manualCheckin(@Valid @RequestBody ManualCheckinRequest request) {
        return Result.success(checkinService.manualCheckin(request));
    }
}
