package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.VenueCreateRequest;
import com.stadium.booking.dto.response.VenueResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "球馆管理", description = "后台球馆管理接口")
@RestController
@RequestMapping("/admin/venues")
@RequiredArgsConstructor
public class VenueAdminController {
    private final VenueService venueService;

    @Operation(summary = "分页查询球馆")
    @GetMapping
    @RequirePermission("venue:read")
    public Result<IPage<VenueResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) Integer status) {
        return Result.success(venueService.listPage(current, size, sportType, status));
    }

    @Operation(summary = "获取球馆详情")
    @GetMapping("/{id}")
    @RequirePermission("venue:read")
    public Result<VenueResponse> getById(@PathVariable Long id) {
        return Result.success(venueService.getById(id));
    }

    @Operation(summary = "创建球馆")
    @PostMapping
    @RequirePermission("venue:create")
    public Result<VenueResponse> create(@Valid @RequestBody VenueCreateRequest request) {
        return Result.success(venueService.create(request));
    }

    @Operation(summary = "更新球馆")
    @PutMapping("/{id}")
    @RequirePermission("venue:update")
    public Result<VenueResponse> update(@PathVariable Long id, @Valid @RequestBody VenueCreateRequest request) {
        return Result.success(venueService.update(id, request));
    }

    @Operation(summary = "更新球馆状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("venue:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        venueService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除球馆")
    @DeleteMapping("/{id}")
    @RequirePermission("venue:delete")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return Result.success();
    }
}
