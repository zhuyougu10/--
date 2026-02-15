package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.CourtCreateRequest;
import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "场地管理", description = "后台场地管理接口")
@RestController
@RequestMapping("/admin/courts")
@RequiredArgsConstructor
public class CourtAdminController {
    private final CourtService courtService;

    @Operation(summary = "分页查询场地")
    @GetMapping
    @RequirePermission("court:read")
    public Result<IPage<CourtResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer status) {
        return Result.success(courtService.listPage(current, size, venueId, status));
    }

    @Operation(summary = "获取球馆下的场地列表")
    @GetMapping({"/venue/{venueId}", "/by-venue/{venueId}"})
    @RequirePermission("court:read")
    public Result<List<CourtResponse>> listByVenue(@PathVariable Long venueId) {
        return Result.success(courtService.listByVenue(venueId));
    }

    @Operation(summary = "获取场地详情")
    @GetMapping("/{id}")
    @RequirePermission("court:read")
    public Result<CourtResponse> getById(@PathVariable Long id) {
        return Result.success(courtService.getById(id));
    }

    @Operation(summary = "创建场地")
    @PostMapping
    @RequirePermission("court:create")
    public Result<CourtResponse> create(@Valid @RequestBody CourtCreateRequest request) {
        return Result.success(courtService.create(request));
    }

    @Operation(summary = "更新场地")
    @PutMapping("/{id}")
    @RequirePermission("court:update")
    public Result<CourtResponse> update(@PathVariable Long id, @Valid @RequestBody CourtCreateRequest request) {
        return Result.success(courtService.update(id, request));
    }

    @Operation(summary = "更新场地状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("court:update")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        courtService.updateStatus(id, status, reason);
        return Result.success();
    }

    @Operation(summary = "删除场地")
    @DeleteMapping("/{id}")
    @RequirePermission("court:delete")
    public Result<Void> delete(@PathVariable Long id) {
        courtService.delete(id);
        return Result.success();
    }
}
