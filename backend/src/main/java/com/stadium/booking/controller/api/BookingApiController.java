package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.BookingCancelRequest;
import com.stadium.booking.dto.request.BookingCreateRequest;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "预约API", description = "小程序端预约接口")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {
    private final BookingService bookingService;

    @Operation(summary = "创建预约")
    @PostMapping
    public Result<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(bookingService.createBooking(userId, request));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/{bookingNo}/cancel")
    public Result<BookingResponse> cancelBooking(
            @PathVariable String bookingNo,
            @RequestBody(required = false) BookingCancelRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(bookingService.cancelBooking(userId, bookingNo, request));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{bookingNo}")
    public Result<BookingResponse> getBooking(@PathVariable String bookingNo) {
        return Result.success(bookingService.getBookingByNo(bookingNo));
    }

    @Operation(summary = "获取我的预约列表")
    @GetMapping("/my")
    public Result<List<BookingResponse>> getMyBookings() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(bookingService.getUserBookings(userId));
    }

    @Operation(summary = "获取我的有效预约")
    @GetMapping("/my/active")
    public Result<List<BookingResponse>> getMyActiveBookings() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(bookingService.getUserActiveBookings(userId));
    }
}
