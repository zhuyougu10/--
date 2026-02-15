package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.BookingCreateRequest;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public void validateBooking(Long userId, BookingCreateRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        validateVenueStatus(venue);
        validateUserStatus(user);
        validateBookingWindow(request.getBookingDate(), venue);
        validateOpenHours(request.getStartTime(), request.getEndTime(), venue);
        validateOpenDay(request.getBookingDate(), venue);
        validateTimeSlot(request.getStartTime(), request.getEndTime(), venue);
        
        int newSlotCount = calculateSlotCount(request.getStartTime(), request.getEndTime(), venue.getSlotMinutes());
        validateDailyLimit(userId, request.getBookingDate(), venue, newSlotCount);
        validateWeeklyLimit(userId, request.getBookingDate(), venue, newSlotCount);
    }

    private void validateVenueStatus(Venue venue) {
        if (venue.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "球馆当前未开放");
        }
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == 0) {
            if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_BANNED, 
                    "账号已被禁用至 " + user.getBannedUntil().toLocalDate());
            }
        }
    }

    private void validateBookingWindow(LocalDate bookingDate, Venue venue) {
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.OUT_OF_BOOKING_WINDOW, "不能预约过去的日期");
        }

        LocalDate maxDate = today.plusDays(venue.getBookAheadDays());
        if (bookingDate.isAfter(maxDate)) {
            throw new BusinessException(ErrorCode.OUT_OF_BOOKING_WINDOW, 
                "最多只能提前 " + venue.getBookAheadDays() + " 天预约");
        }
    }

    private void validateOpenHours(LocalTime startTime, LocalTime endTime, Venue venue) {
        if (startTime.isBefore(venue.getOpenTime()) || endTime.isAfter(venue.getCloseTime())) {
            throw new BusinessException(ErrorCode.OUT_OF_OPEN_HOURS, 
                "营业时间: " + venue.getOpenTime() + " - " + venue.getCloseTime());
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "开始时间必须早于结束时间");
        }
    }

    private void validateOpenDay(LocalDate bookingDate, Venue venue) {
        int dayOfWeek = bookingDate.getDayOfWeek().getValue();
        String[] openDays = venue.getOpenDays().split(",");
        boolean isOpen = false;
        for (String day : openDays) {
            if (Integer.parseInt(day.trim()) == dayOfWeek) {
                isOpen = true;
                break;
            }
        }
        if (!isOpen) {
            throw new BusinessException(ErrorCode.OUT_OF_OPEN_HOURS, "当天不开放");
        }
    }

    private void validateTimeSlot(LocalTime startTime, LocalTime endTime, Venue venue) {
        int slotMinutes = venue.getSlotMinutes();
        long duration = ChronoUnit.MINUTES.between(startTime, endTime);
        
        if (duration % slotMinutes != 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, 
                "时段长度必须为 " + slotMinutes + " 分钟的整数倍");
        }

        if (startTime.getMinute() % slotMinutes != 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, 
                "开始时间必须对齐时段边界");
        }
    }

    private void validateDailyLimit(Long userId, LocalDate date, Venue venue, int newSlotCount) {
        int usedSlots = bookingRepository.countSlotsByUserAndDate(userId, date);
        
        if (usedSlots + newSlotCount > venue.getDailySlotLimit()) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, 
                "每日最多预约 " + venue.getDailySlotLimit() + " 个时段，您今日已预约 " + usedSlots + " 个时段");
        }
    }

    private void validateWeeklyLimit(Long userId, LocalDate date, Venue venue, int newSlotCount) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        int usedSlots = bookingRepository.countSlotsByUserAndDateRange(userId, weekStart, weekEnd);
        
        if (usedSlots + newSlotCount > venue.getWeeklySlotLimit()) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, 
                "每周最多预约 " + venue.getWeeklySlotLimit() + " 个时段，您本周已预约 " + usedSlots + " 个时段");
        }
    }

    public void validateCancellation(Long userId, Booking booking, Venue venue) {
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能取消已确认的预约");
        }

        LocalDateTime bookingStart = booking.getBookingDate().atTime(booking.getStartTime());
        LocalDateTime cutoffTime = bookingStart.minusMinutes(venue.getCancelCutoffMinutes());

        if (LocalDateTime.now().isAfter(cutoffTime)) {
            throw new BusinessException(ErrorCode.CANCEL_NOT_ALLOWED, 
                "开始前 " + venue.getCancelCutoffMinutes() + " 分钟内不可取消");
        }
    }

    public void validateTimeOrder(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "开始时间必须早于结束时间");
        }
    }

    private int calculateSlotCount(LocalTime startTime, LocalTime endTime, int slotMinutes) {
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        return (int) (minutes / slotMinutes);
    }
}
