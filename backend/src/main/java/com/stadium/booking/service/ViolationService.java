package com.stadium.booking.service;

import com.stadium.booking.common.enums.BookingStatus;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.MarkNoShowRequest;
import com.stadium.booking.dto.response.ViolationResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.entity.ViolationRecord;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.repository.ViolationRecordRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViolationService {
    private final ViolationRecordRepository violationRecordRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final AuditService auditService;
    private final AdminVenueAccessService adminVenueAccessService;

    private static final int NO_SHOW_THRESHOLD = 3;
    private static final int BAN_DAYS = 7;
    private static final int VIOLATION_WINDOW_DAYS = 30;

    @Transactional
    public ViolationResponse markNoShow(MarkNoShowRequest request) {
        Booking booking = bookingRepository.findByBookingNo(request.getBookingNo())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));
        adminVenueAccessService.checkVenueAccess(booking.getVenueId());

        if (booking.getStatus() != BookingStatus.CONFIRMED.getCode()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能标记已确认的预约为爽约");
        }

        Venue venue = venueRepository.findById(booking.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        Integer graceMinutes = venue.getNoShowGraceMinutes();
        if (graceMinutes == null) {
            graceMinutes = 15;
        }

        LocalDateTime gracePeriodEnd = booking.getBookingDate()
            .atTime(booking.getStartTime())
            .plusMinutes(graceMinutes);

        if (LocalDateTime.now().isBefore(gracePeriodEnd)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "宽限期未结束，无法标记爽约");
        }

        ViolationRecord existingRecord = violationRecordRepository.findByBookingId(booking.getId());
        if (existingRecord != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "该预约已被标记为爽约");
        }

        Long operatorId = UserContext.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        ViolationRecord record = new ViolationRecord();
        record.setUserId(booking.getUserId());
        record.setBookingId(booking.getId());
        record.setViolationType(1);
        record.setBookingDate(booking.getBookingDate());
        record.setMarkedBy(operatorId);
        record.setMarkedAt(now);
        violationRecordRepository.insert(record);

        booking.setStatus(BookingStatus.NO_SHOW.getCode());
        booking.setNoShowMarkedAt(now);
        booking.setNoShowMarkedBy(operatorId);
        bookingRepository.updateById(booking);

        User user = userRepository.findById(booking.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        Integer currentNoShowCount = user.getNoShowCount();
        if (currentNoShowCount == null) {
            currentNoShowCount = 0;
        }
        user.setNoShowCount(currentNoShowCount + 1);
        user.setLastNoShowAt(now);

        LocalDate windowStart = LocalDate.now().minusDays(VIOLATION_WINDOW_DAYS);
        int recentViolations = violationRecordRepository.countRecentViolations(user.getId(), windowStart);

        if (recentViolations >= NO_SHOW_THRESHOLD) {
            LocalDateTime banUntil = now.plusDays(BAN_DAYS);
            user.setStatus(0);
            user.setBannedUntil(banUntil);
            record.setBanDays(BAN_DAYS);
            record.setBanUntil(banUntil);
            violationRecordRepository.updateById(record);

            log.warn("User {} banned until {} due to {} violations", 
                user.getId(), banUntil, recentViolations);
        }

        userRepository.updateById(user);

        auditService.log("MARK_NO_SHOW", "booking", booking.getId(), booking.getBookingNo(), null, record);

        return toResponse(record, user, booking);
    }

    @Transactional
    public void clearViolation(Long violationId) {
        ViolationRecord record = violationRecordRepository.selectById(violationId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "违约记录不存在");
        }

        if (record.getClearedAt() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "该违约记录已清除");
        }

        Long operatorId = UserContext.getCurrentUserId();
        violationRecordRepository.clearViolation(violationId, operatorId);

        User user = userRepository.findById(record.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        LocalDate windowStart = LocalDate.now().minusDays(VIOLATION_WINDOW_DAYS);
        int remainingViolations = violationRecordRepository.countRecentViolations(user.getId(), windowStart);

        if (remainingViolations < NO_SHOW_THRESHOLD && user.getStatus() != null && user.getStatus() == 0) {
            user.setStatus(1);
            user.setBannedUntil(null);
            userRepository.updateById(user);
        }

        auditService.log("CLEAR_VIOLATION", "violation", record.getId(), null, record, null);
    }

    public List<ViolationResponse> getUserViolations(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        return violationRecordRepository.findByUserId(userId).stream()
            .map(record -> {
                Booking booking = bookingRepository.findById(record.getBookingId()).orElse(null);
                return toResponse(record, user, booking);
            })
            .collect(Collectors.toList());
    }

    public ViolationResponse getViolationById(Long id) {
        ViolationRecord record = violationRecordRepository.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "违约记录不存在");
        }

        User user = userRepository.findById(record.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        Booking booking = bookingRepository.findById(record.getBookingId()).orElse(null);
        return toResponse(record, user, booking);
    }

    private ViolationResponse toResponse(ViolationRecord record, User user, Booking booking) {
        ViolationResponse response = new ViolationResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setUserName(user != null ? user.getName() : null);
        response.setBookingId(record.getBookingId());
        response.setBookingNo(booking != null ? booking.getBookingNo() : null);
        response.setViolationType(record.getViolationType());
        response.setViolationTypeText(getViolationTypeText(record.getViolationType()));
        response.setBookingDate(record.getBookingDate());
        response.setMarkedAt(record.getMarkedAt());
        response.setBanDays(record.getBanDays());
        response.setBanUntil(record.getBanUntil());
        response.setClearedAt(record.getClearedAt());
        return response;
    }

    private String getViolationTypeText(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "爽约";
            case 2 -> "超时取消";
            default -> "未知";
        };
    }
}
