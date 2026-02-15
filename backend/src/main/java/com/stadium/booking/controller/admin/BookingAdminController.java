package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "预约管理", description = "后台预约管理接口")
@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class BookingAdminController {
    private final BookingService bookingService;

    @Operation(summary = "分页查询预约")
    @GetMapping
    @RequirePermission("booking:read")
    public Result<IPage<BookingResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long courtId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer status) {
        return Result.success(bookingService.listPage(current, size, venueId, courtId, userId, date, status));
    }

    @Operation(summary = "获取今日预约")
    @GetMapping("/today")
    @RequirePermission("booking:read")
    public Result<List<BookingResponse>> getTodayBookings(
            @RequestParam(required = false) Long venueId) {
        return Result.success(bookingService.getTodayBookings(venueId));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{bookingNoOrId}")
    @RequirePermission("booking:read")
    public Result<BookingResponse> getById(@PathVariable String bookingNoOrId) {
        if (bookingNoOrId.startsWith("BK")) {
            return Result.success(bookingService.getBookingByNo(bookingNoOrId));
        }
        return Result.success(bookingService.getBookingById(Long.parseLong(bookingNoOrId)));
    }

    @Operation(summary = "获取球馆当日预约")
    @GetMapping("/venue/{venueId}")
    @RequirePermission("booking:read")
    public Result<List<BookingResponse>> getVenueBookings(
            @PathVariable Long venueId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(bookingService.getVenueBookings(venueId, date));
    }

    @Operation(summary = "管理员取消预约")
    @PostMapping("/{bookingNo}/cancel")
    @RequirePermission("booking:cancel")
    public Result<BookingResponse> cancelBooking(
            @PathVariable String bookingNo,
            @RequestParam(required = false) String reason) {
        Long adminId = UserContext.getCurrentUserId();
        return Result.success(bookingService.adminCancelBooking(adminId, bookingNo, reason));
    }
}
