package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.TimeSlotResponse;
import com.stadium.booking.dto.response.VenueResponse;
import com.stadium.booking.service.TimeSlotService;
import com.stadium.booking.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "球馆API", description = "小程序端球馆查询接口")
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueApiController {
    private final VenueService venueService;
    private final TimeSlotService timeSlotService;

    @Operation(summary = "获取球馆列表")
    @GetMapping
    public Result<List<VenueResponse>> list() {
        return Result.success(venueService.listAll());
    }

    @Operation(summary = "获取球馆详情")
    @GetMapping("/{id}")
    public Result<VenueResponse> getById(@PathVariable Long id) {
        return Result.success(venueService.getById(id));
    }

    @Operation(summary = "获取场地可用时段")
    @GetMapping("/{venueId}/courts/{courtId}/slots")
    public Result<List<TimeSlotResponse>> getSlots(
            @PathVariable Long venueId,
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(timeSlotService.getAvailableSlots(venueId, courtId, date));
    }
}
