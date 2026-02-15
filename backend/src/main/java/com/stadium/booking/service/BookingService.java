package com.stadium.booking.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.enums.BookingStatus;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.BookingCancelRequest;
import com.stadium.booking.dto.request.BookingCreateRequest;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final BookingValidator bookingValidator;
    private final AuditService auditService;

    @Transactional
    public BookingResponse createBooking(Long userId, BookingCreateRequest request) {
        bookingValidator.validateBooking(userId, request);

        Venue venue = venueRepository.findById(request.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        Court court = courtRepository.findById(request.getCourtId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));

        if (court.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "场地当前不可用");
        }

        int conflicts = bookingRepository.countConflictingBookings(
            request.getCourtId(),
            request.getBookingDate(),
            request.getStartTime().toString(),
            request.getEndTime().toString()
        );

        if (conflicts > 0) {
            throw new BusinessException(ErrorCode.SLOT_CONFLICT, "时段已被占用");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        Booking booking = new Booking();
        booking.setBookingNo(generateBookingNo());
        booking.setUserId(userId);
        booking.setUserName(user.getName());
        booking.setUserPhone(user.getPhone());
        booking.setVenueId(venue.getId());
        booking.setVenueName(venue.getName());
        booking.setCourtId(court.getId());
        booking.setCourtName(court.getName());
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setSlotCount(calculateSlotCount(request.getStartTime(), request.getEndTime(), venue.getSlotMinutes()));
        booking.setBookingType(request.getBookingType());
        booking.setStatus(BookingStatus.CONFIRMED.getCode());
        booking.setRemark(request.getRemark());

        bookingRepository.insert(booking);

        auditService.log("CREATE", "booking", booking.getId(), booking.getBookingNo(), null, booking);

        log.info("Booking created: {} for user {} on {} {}-{}", 
            booking.getBookingNo(), userId, request.getBookingDate(), 
            request.getStartTime(), request.getEndTime());

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long userId, String bookingNo, BookingCancelRequest request) {
        Booking booking = bookingRepository.findByBookingNo(bookingNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        if (!booking.getUserId().equals(userId) && !UserContext.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权取消此预约");
        }

        Venue venue = venueRepository.findById(booking.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        bookingValidator.validateCancellation(userId, booking, venue);

        Booking oldBooking = new Booking();
        oldBooking.setStatus(booking.getStatus());

        String reason = request != null ? request.getReason() : null;
        int cancelledByType = UserContext.isCurrentUserAdmin() ? 3 : 1;

        bookingRepository.cancelBooking(booking.getId(), reason, userId, cancelledByType);

        auditService.log("CANCEL", "booking", booking.getId(), booking.getBookingNo(), oldBooking, 
            java.util.Map.of("status", 2, "reason", reason));

        log.info("Booking cancelled: {} by user {}", bookingNo, userId);

        booking.setStatus(BookingStatus.CANCELLED.getCode());
        booking.setCancelReason(reason);
        return toResponse(booking);
    }

    public BookingResponse getBookingByNo(String bookingNo) {
        Booking booking = bookingRepository.findByBookingNo(bookingNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));
        return toResponse(booking);
    }

    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));
        return toResponse(booking);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getUserActiveBookings(Long userId) {
        return bookingRepository.findByUserIdAndStatus(userId, BookingStatus.CONFIRMED.getCode()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getVenueBookings(Long venueId, LocalDate date) {
        return bookingRepository.findByVenueAndDate(venueId, date).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getTodayBookings(Long venueId) {
        LocalDate today = LocalDate.now();
        if (venueId != null) {
            return getVenueBookings(venueId, today);
        }
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getBookingDate, today)
               .orderByAsc(Booking::getStartTime);
        return bookingRepository.selectList(wrapper).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public IPage<BookingResponse> listPage(Integer current, Integer size, Long venueId, 
            Long courtId, Long userId, LocalDate date, Integer status) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        
        if (venueId != null) {
            wrapper.eq(Booking::getVenueId, venueId);
        }
        if (courtId != null) {
            wrapper.eq(Booking::getCourtId, courtId);
        }
        if (userId != null) {
            wrapper.eq(Booking::getUserId, userId);
        }
        if (date != null) {
            wrapper.eq(Booking::getBookingDate, date);
        }
        if (status != null) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreatedAt);

        IPage<Booking> page = bookingRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toResponse);
    }

    @Transactional
    public BookingResponse adminCancelBooking(Long adminId, String bookingNo, String reason) {
        Booking booking = bookingRepository.findByBookingNo(bookingNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        if (booking.getStatus() != BookingStatus.CONFIRMED.getCode()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能取消已确认的预约");
        }

        Booking oldBooking = new Booking();
        oldBooking.setStatus(booking.getStatus());

        bookingRepository.cancelBooking(booking.getId(), reason, adminId, 3);

        auditService.log("ADMIN_CANCEL", "booking", booking.getId(), booking.getBookingNo(), 
            oldBooking, java.util.Map.of("status", 2, "reason", reason));

        log.info("Booking admin cancelled: {} by admin {}", bookingNo, adminId);

        booking.setStatus(BookingStatus.CANCELLED.getCode());
        booking.setCancelReason(reason);
        return toResponse(booking);
    }

    private String generateBookingNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
        return "BK" + datePart + randomPart;
    }

    private int calculateSlotCount(LocalTime startTime, LocalTime endTime, int slotMinutes) {
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
        return (int) (minutes / slotMinutes);
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingNo(booking.getBookingNo());
        response.setUserId(booking.getUserId());
        response.setUserName(booking.getUserName());
        response.setUserPhone(booking.getUserPhone());
        response.setVenueId(booking.getVenueId());
        response.setVenueName(booking.getVenueName());
        response.setCourtId(booking.getCourtId());
        response.setCourtName(booking.getCourtName());
        response.setBookingDate(booking.getBookingDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setSlotCount(booking.getSlotCount());
        response.setBookingType(booking.getBookingType());
        response.setStatus(booking.getStatus());
        response.setStatusText(getStatusText(booking.getStatus()));
        response.setCancelReason(booking.getCancelReason());
        response.setCancelledAt(booking.getCancelledAt());
        response.setCheckedInAt(booking.getCheckedInAt());
        response.setRemark(booking.getRemark());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "已确认";
            case 2 -> "已取消";
            case 3 -> "已签到";
            case 4 -> "爽约";
            default -> "未知";
        };
    }
}
