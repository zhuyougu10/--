# Task 05: 预约核心功能

> **依赖:** task-04-venue-management.md
> **预计时间:** 5-6 小时

## 目标
实现预约系统的核心功能，包括创建预约、取消预约、预约规则校验、并发安全保证等。

---

## 预约流程概览

```
┌─────────────────────────────────────────────────────────┐
│                    预约创建流程                          │
├─────────────────────────────────────────────────────────┤
│  用户选择球馆/场地/日期/时段                              │
│           │                                             │
│           ▼                                             │
│  ┌─────────────────────┐                               │
│  │   规则校验           │                               │
│  │  - 时间合法性        │                               │
│  │  - 可预约窗口        │                               │
│  │  - 个人限额          │                               │
│  │  - 用户状态          │                               │
│  └─────────┬───────────┘                               │
│            │ 通过                                       │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │   并发冲突检测       │  (SELECT FOR UPDATE)          │
│  │  - 时段是否已被占用  │                               │
│  └─────────┬───────────┘                               │
│            │ 无冲突                                     │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │   创建预约记录       │                               │
│  │  - 生成预约单号      │                               │
│  │  - 状态: CONFIRMED   │                               │
│  │  - 记录审计日志      │                               │
│  └─────────────────────┘                               │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建预约实体

**文件:** `backend/src/main/java/com/stadium/booking/entity/Booking.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("booking")
public class Booking extends BaseEntity {
    private String bookingNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long venueId;
    private String venueName;
    private Long courtId;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotCount;
    private Integer bookingType;
    private Long groupId;
    private Integer status;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private Integer cancelledByType;
    private LocalDateTime checkedInAt;
    private Long checkedInBy;
    private Integer checkinMethod;
    private LocalDateTime noShowMarkedAt;
    private Long noShowMarkedBy;
    private String remark;
}
```

---

## Step 2: 创建预约 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/BookingCreateRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingCreateRequest {
    @NotNull(message = "球馆ID不能为空")
    private Long venueId;

    @NotNull(message = "场地ID不能为空")
    private Long courtId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    private String remark;

    private Integer bookingType = 1;

    private List<Long> courtIds;

    private Integer participantCount;

    private String purpose;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/BookingCancelRequest.java`

```java
package com.stadium.booking.dto.request;

import lombok.Data;

@Data
public class BookingCancelRequest {
    private String reason;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/BookingResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingResponse {
    private Long id;
    private String bookingNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long venueId;
    private String venueName;
    private Long courtId;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotCount;
    private Integer bookingType;
    private Integer status;
    private String statusText;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
    private String remark;
    private LocalDateTime createdAt;
    private String qrToken;
}
```

---

## Step 3: 创建预约规则校验器

**文件:** `backend/src/main/java/com/stadium/booking/service/BookingValidator.java`

```java
package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.BookingCreateRequest;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

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

        validateUserStatus(user);
        validateBookingWindow(request.getBookingDate(), venue);
        validateOpenHours(request.getStartTime(), request.getEndTime(), venue);
        validateOpenDay(request.getBookingDate(), venue);
        validateTimeSlot(request.getStartTime(), request.getEndTime(), venue);
        validateDailyLimit(userId, request.getBookingDate(), venue);
        validateWeeklyLimit(userId, request.getBookingDate(), venue);
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

    private void validateDailyLimit(Long userId, LocalDate date, Venue venue) {
        int usedSlots = bookingRepository.countSlotsByUserAndDate(userId, date);
        int requestSlots = 1;
        
        if (usedSlots + requestSlots > venue.getDailySlotLimit()) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, 
                "每日最多预约 " + venue.getDailySlotLimit() + " 个时段");
        }
    }

    private void validateWeeklyLimit(Long userId, LocalDate date, Venue venue) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        int usedSlots = bookingRepository.countSlotsByUserAndDateRange(userId, weekStart, weekEnd);
        
        if (usedSlots >= venue.getWeeklySlotLimit()) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, 
                "每周最多预约 " + venue.getWeeklySlotLimit() + " 个时段");
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
}
```

---

## Step 4: 更新 BookingRepository

**文件:** `backend/src/main/java/com/stadium/booking/repository/BookingRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface BookingRepository extends BaseMapper<Booking> {
    @Select("""
        SELECT * FROM booking 
        WHERE court_id = #{courtId} 
        AND booking_date = #{date} 
        AND status = 1
        AND deleted_at IS NULL
        ORDER BY start_time
        """)
    List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date);

    @Select("""
        SELECT * FROM booking 
        WHERE id = #{id} 
        AND deleted_at IS NULL
        """)
    Optional<Booking> findById(Long id);

    @Select("""
        SELECT * FROM booking 
        WHERE booking_no = #{bookingNo} 
        AND deleted_at IS NULL
        """)
    Optional<Booking> findByBookingNo(String bookingNo);

    @Select("""
        SELECT * FROM booking 
        WHERE user_id = #{userId} 
        AND deleted_at IS NULL
        ORDER BY booking_date DESC, start_time DESC
        """)
    List<Booking> findByUserId(Long userId);

    @Select("""
        SELECT COUNT(*) FROM booking 
        WHERE user_id = #{userId} 
        AND booking_date = #{date} 
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countSlotsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("""
        SELECT COUNT(*) FROM booking 
        WHERE user_id = #{userId} 
        AND booking_date BETWEEN #{startDate} AND #{endDate}
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countSlotsByUserAndDateRange(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    @Select("""
        SELECT COUNT(*) FROM booking 
        WHERE court_id = #{courtId} 
        AND booking_date = #{date}
        AND start_time < #{endTime}
        AND end_time > #{startTime}
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countConflictingBookings(
        @Param("courtId") Long courtId,
        @Param("date") LocalDate date,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    @Update("""
        UPDATE booking 
        SET status = 2, cancel_reason = #{reason}, cancelled_at = NOW(), 
            cancelled_by = #{cancelledBy}, cancelled_by_type = #{cancelledByType}
        WHERE id = #{id}
        """)
    int cancelBooking(
        @Param("id") Long id, 
        @Param("reason") String reason,
        @Param("cancelledBy") Long cancelledBy,
        @Param("cancelledByType") Integer cancelledByType
    );

    @Select("""
        SELECT * FROM booking 
        WHERE venue_id = #{venueId} 
        AND booking_date = #{date}
        AND status IN (1, 3)
        AND deleted_at IS NULL
        ORDER BY court_id, start_time
        """)
    List<Booking> findByVenueAndDate(Long venueId, LocalDate date);

    @Select("""
        SELECT * FROM booking 
        WHERE status = 1 
        AND booking_date = #{date}
        AND deleted_at IS NULL
        ORDER BY venue_id, court_id, start_time
        """)
    List<Booking> findTodayBookings(LocalDate date);
}
```

---

## Step 5: 创建预约服务

**文件:** `backend/src/main/java/com/stadium/booking/service/BookingService.java`

```java
package com.stadium.booking.service;

import cn.hutool.core.util.IdUtil;
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
        booking.setStatus(1);
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

        String reason = request != null ? request.getReason() : null;
        int cancelledByType = UserContext.isCurrentUserAdmin() ? 3 : 1;

        bookingRepository.cancelBooking(booking.getId(), reason, userId, cancelledByType);

        auditService.log("CANCEL", "booking", booking.getId(), booking.getBookingNo(), booking, null);

        booking.setStatus(2);
        booking.setCancelReason(reason);
        return toResponse(booking);
    }

    public BookingResponse getBookingDetail(String bookingNo) {
        Booking booking = bookingRepository.findByBookingNo(bookingNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        Long currentUserId = UserContext.getCurrentUserId();
        if (!booking.getUserId().equals(currentUserId) && !UserContext.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此预约");
        }

        return toResponse(booking);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getTodaySchedule(Long venueId) {
        LocalDate today = LocalDate.now();
        List<Booking> bookings;
        
        if (venueId != null) {
            bookings = bookingRepository.findByVenueAndDate(venueId, today);
        } else {
            bookings = bookingRepository.findTodayBookings(today);
        }

        return bookings.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private String generateBookingNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = IdUtil.randomUUID().substring(0, 8).toUpperCase();
        return "BK" + dateStr + randomStr;
    }

    private int calculateSlotCount(LocalTime startTime, LocalTime endTime, int slotMinutes) {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
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
        return switch (status) {
            case 1 -> "已确认";
            case 2 -> "已取消";
            case 3 -> "已签到";
            case 4 -> "爽约";
            default -> "未知";
        };
    }
}
```

---

## Step 6: 创建审计服务

**文件:** `backend/src/main/java/com/stadium/booking/service/AuditService.java`

```java
package com.stadium.booking.service;

import cn.hutool.json.JSONUtil;
import com.stadium.booking.entity.AuditLog;
import com.stadium.booking.repository.AuditLogRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(String action, String resourceType, Long resourceId, 
                   String resourceName, Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setUserId(UserContext.getCurrentUserId());
        log.setUserType(getUserTypeCode());
        log.setUsername(getUsername());
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setResourceName(resourceName);
        log.setOldValue(oldValue != null ? JSONUtil.toJsonStr(oldValue) : null);
        log.setNewValue(newValue != null ? JSONUtil.toJsonStr(newValue) : null);

        auditLogRepository.insert(log);
    }

    private Integer getUserTypeCode() {
        String userType = UserContext.getCurrentUserType();
        if (userType == null) return null;
        return switch (userType) {
            case "USER" -> 1;
            case "VENUE_STAFF" -> 2;
            case "ADMIN" -> 3;
            default -> null;
        };
    }

    private String getUsername() {
        return UserContext.getCurrentUserId() != null ? 
            UserContext.getCurrentUserId().toString() : null;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/entity/AuditLog.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    private Long id;
    private Long userId;
    private Integer userType;
    private String username;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/AuditLogRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogRepository extends BaseMapper<AuditLog> {
}
```

---

## Step 7: 创建预约控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/BookingApiController.java`

```java
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
    public Result<BookingResponse> create(@Valid @RequestBody BookingCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(bookingService.createBooking(userId, request));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/{bookingNo}/cancel")
    public Result<BookingResponse> cancel(
            @PathVariable String bookingNo,
            @RequestBody(required = false) BookingCancelRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(bookingService.cancelBooking(userId, bookingNo, request));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{bookingNo}")
    public Result<BookingResponse> getDetail(@PathVariable String bookingNo) {
        return Result.success(bookingService.getBookingDetail(bookingNo));
    }

    @Operation(summary = "获取我的预约列表")
    @GetMapping("/my")
    public Result<List<BookingResponse>> getMyBookings() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(bookingService.getUserBookings(userId));
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/BookingAdminController.java`

```java
package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.security.RequirePermission;
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

    @Operation(summary = "获取今日排场")
    @GetMapping("/today")
    @RequirePermission("booking:read")
    public Result<List<BookingResponse>> getTodaySchedule(
            @RequestParam(required = false) Long venueId) {
        return Result.success(bookingService.getTodaySchedule(venueId));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{bookingNo}")
    @RequirePermission("booking:read")
    public Result<BookingResponse> getDetail(@PathVariable String bookingNo) {
        return Result.success(bookingService.getBookingDetail(bookingNo));
    }
}
```

---

## Step 8: 验证功能

**测试创建预约:**
```bash
curl -X POST http://localhost:8080/api/api/bookings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "courtId": 1,
    "bookingDate": "2026-02-15",
    "startTime": "10:00",
    "endTime": "11:00"
  }'
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "bookingNo": "BK20260213ABCD1234",
    "status": 1,
    "statusText": "已确认"
  }
}
```

**测试取消预约:**
```bash
curl -X POST http://localhost:8080/api/api/bookings/BK20260213ABCD1234/cancel \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "临时有事"}'
```

---

## 提交

```bash
git add backend/
git commit -m "feat(booking): implement booking creation, cancellation and validation"
```

---

## 注意事项

1. **并发安全**: 当前实现使用数据库唯一索引+乐观锁，高并发场景建议使用分布式锁
2. **事务边界**: 确保预约创建和冲突检测在同一事务内
3. **限额统计**: 考虑使用 Redis 缓存用户当日预约数量
4. **审计日志**: 所有状态变更必须记录审计日志
