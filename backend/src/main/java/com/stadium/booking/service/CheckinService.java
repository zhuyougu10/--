package com.stadium.booking.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.CheckinRequest;
import com.stadium.booking.dto.request.ManualCheckinRequest;
import com.stadium.booking.dto.response.CheckinResponse;
import com.stadium.booking.dto.response.QrCodeResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.CheckinRecord;
import com.stadium.booking.entity.QrToken;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CheckinRecordRepository;
import com.stadium.booking.repository.QrTokenRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {
    private final QrTokenRepository qrTokenRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final AuditService auditService;

    private static final int QR_TOKEN_VALID_MINUTES = 5;

    public IPage<CheckinResponse> listRecords(Integer current, Integer size, Long venueId, LocalDate date) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (venueId != null) {
            wrapper.eq(CheckinRecord::getVenueId, venueId);
        }
        if (date != null) {
            wrapper.apply("DATE(checked_in_at) = {0}", date);
        }
        wrapper.orderByDesc(CheckinRecord::getCheckedInAt);

        IPage<CheckinRecord> page = checkinRecordRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(record -> {
            Booking booking = bookingRepository.findById(record.getBookingId()).orElse(null);
            return buildCheckinResponse(booking != null ? booking : new Booking(), true, "已核销");
        });
    }

    public QrCodeResponse generateQrToken(Long userId, String bookingNo) {
        Booking booking = bookingRepository.findByBookingNo(bookingNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此预约");
        }

        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "预约状态不正确");
        }

        QrToken existingToken = qrTokenRepository.findActiveByBookingId(booking.getId()).orElse(null);
        if (existingToken != null) {
            QrCodeResponse response = new QrCodeResponse();
            response.setToken(existingToken.getToken());
            response.setQrData(existingToken.getToken());
            response.setExpiresAt(existingToken.getExpiresAt());
            return response;
        }

        String token = "QR" + IdUtil.fastSimpleUUID().toUpperCase();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(QR_TOKEN_VALID_MINUTES);

        QrToken qrToken = new QrToken();
        qrToken.setToken(token);
        qrToken.setBookingId(booking.getId());
        qrToken.setUserId(userId);
        qrToken.setExpiresAt(expiresAt);
        qrTokenRepository.insert(qrToken);

        QrCodeResponse response = new QrCodeResponse();
        response.setToken(token);
        response.setQrData(token);
        response.setExpiresAt(expiresAt);
        return response;
    }

    @Transactional
    public CheckinResponse checkin(CheckinRequest request) {
        QrToken qrToken = qrTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "无效的核销码"));

        if (qrToken.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "核销码已使用");
        }

        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "核销码已过期");
        }

        Booking booking = bookingRepository.findById(qrToken.getBookingId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        return performCheckin(booking, qrToken.getToken(), request.getCheckinMethod());
    }

    @Transactional
    public CheckinResponse manualCheckin(ManualCheckinRequest request) {
        Booking booking = bookingRepository.findByBookingNo(request.getBookingNo())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        return performCheckin(booking, null, 2);
    }

    private CheckinResponse performCheckin(Booking booking, String qrToken, Integer checkinMethod) {
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "预约状态不正确，无法核销");
        }

        Venue venue = venueRepository.findById(booking.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        validateCheckinWindow(booking, venue);

        CheckinRecord existingRecord = checkinRecordRepository.findByBookingId(booking.getId()).orElse(null);
        if (existingRecord != null) {
            log.info("Booking already checked in: {}", booking.getBookingNo());
            return buildCheckinResponse(booking, true, "该预约已核销");
        }

        Long operatorId = UserContext.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        CheckinRecord record = new CheckinRecord();
        record.setBookingId(booking.getId());
        record.setBookingNo(booking.getBookingNo());
        record.setQrToken(qrToken);
        record.setCheckinMethod(checkinMethod);
        record.setCheckedInBy(operatorId);
        record.setCheckedInAt(now);
        record.setVenueId(booking.getVenueId());
        checkinRecordRepository.insert(record);

        booking.setStatus(3);
        booking.setCheckedInAt(now);
        booking.setCheckedInBy(operatorId);
        booking.setCheckinMethod(checkinMethod);
        bookingRepository.updateById(booking);

        if (qrToken != null) {
            qrTokenRepository.findByToken(qrToken).ifPresent(token -> 
                qrTokenRepository.markAsUsed(token.getId(), now)
            );
        }

        auditService.log("CHECKIN", "booking", booking.getId(), booking.getBookingNo(), null, record);

        log.info("Booking checked in: {} by user {}", booking.getBookingNo(), operatorId);

        return buildCheckinResponse(booking, true, "核销成功");
    }

    private void validateCheckinWindow(Booking booking, Venue venue) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingStart = booking.getBookingDate().atTime(booking.getStartTime());
        LocalDateTime bookingEnd = booking.getBookingDate().atTime(booking.getEndTime());

        LocalDateTime windowStart = bookingStart.minusMinutes(venue.getCheckinWindowBefore());

        if (now.isBefore(windowStart)) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_ALLOWED, 
                "核销窗口未开启，请在开始前" + venue.getCheckinWindowBefore() + "分钟内核销");
        }

        if (now.isAfter(bookingEnd)) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_ALLOWED, "预约已结束，无法核销");
        }
    }

    private CheckinResponse buildCheckinResponse(Booking booking, boolean success, String message) {
        CheckinResponse response = new CheckinResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setBookingNo(booking.getBookingNo());
        response.setVenueName(booking.getVenueName());
        response.setCourtName(booking.getCourtName());
        response.setBookingDate(booking.getBookingDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setUserName(booking.getUserName());
        response.setCheckedInAt(booking.getCheckedInAt());
        return response;
    }
}
