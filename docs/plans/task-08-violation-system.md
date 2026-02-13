# Task 08: 违约与限制系统

> **依赖:** task-05-booking-core.md
> **预计时间:** 2-3 小时

## 目标
实现违约记录管理和限制策略，包括爽约标记、违约记录、自动禁用预约功能等。

---

## 违约处理流程

```
┌─────────────────────────────────────────────────────────┐
│                    违约处理流程                          │
├─────────────────────────────────────────────────────────┤
│  预约开始时间 + 宽限期                                   │
│           │                                             │
│           ▼                                             │
│  用户未核销？                                            │
│           │                                             │
│     ┌─────┴─────┐                                       │
│     │ 是        │ 否                                    │
│     ▼           ▼                                       │
│  场馆员标记    正常签到                                  │
│  爽约(NO_SHOW)                                          │
│     │                                                   │
│     ▼                                                   │
│  ┌─────────────────────┐                               │
│  │   记录违约           │                               │
│  │  - 更新爽约次数      │                               │
│  │  - 检查阈值          │                               │
│  └─────────┬───────────┘                               │
│            │ 达到阈值                                   │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │   执行限制           │                               │
│  │  - 禁用预约N天       │                               │
│  │  - 通知用户          │                               │
│  └─────────────────────┘                               │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建违约记录实体和 Repository

**文件:** `backend/src/main/java/com/stadium/booking/entity/ViolationRecord.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("violation_record")
public class ViolationRecord {
    private Long id;
    private Long userId;
    private Long bookingId;
    private Integer violationType;
    private LocalDate bookingDate;
    private Long markedBy;
    private LocalDateTime markedAt;
    private Integer banDays;
    private LocalDateTime banUntil;
    private LocalDateTime clearedAt;
    private Long clearedBy;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/ViolationRecordRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.ViolationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ViolationRecordRepository extends BaseMapper<ViolationRecord> {
    @Select("""
        SELECT COUNT(*) FROM violation_record 
        WHERE user_id = #{userId} 
        AND booking_date >= #{startDate}
        AND cleared_at IS NULL
        """)
    int countRecentViolations(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("""
        SELECT * FROM violation_record 
        WHERE user_id = #{userId} 
        ORDER BY marked_at DESC
        """)
    List<ViolationRecord> findByUserId(Long userId);

    @Select("""
        SELECT * FROM violation_record 
        WHERE booking_id = #{bookingId}
        """)
    ViolationRecord findByBookingId(Long bookingId);

    @Update("""
        UPDATE violation_record 
        SET cleared_at = NOW(), cleared_by = #{clearedBy}
        WHERE id = #{id} AND cleared_at IS NULL
        """)
    int clearViolation(@Param("id") Long id, @Param("clearedBy") Long clearedBy);
}
```

---

## Step 2: 创建违约 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/MarkNoShowRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkNoShowRequest {
    @NotBlank(message = "预约单号不能为空")
    private String bookingNo;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/ViolationResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ViolationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookingId;
    private String bookingNo;
    private Integer violationType;
    private String violationTypeText;
    private LocalDate bookingDate;
    private LocalDateTime markedAt;
    private Integer banDays;
    private LocalDateTime banUntil;
    private LocalDateTime clearedAt;
}
```

---

## Step 3: 创建违约服务

**文件:** `backend/src/main/java/com/stadium/booking/service/ViolationService.java`

```java
package com.stadium.booking.service;

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

    private static final int NO_SHOW_THRESHOLD = 3;
    private static final int BAN_DAYS = 7;
    private static final int VIOLATION_WINDOW_DAYS = 30;

    @Transactional
    public ViolationResponse markNoShow(MarkNoShowRequest request) {
        Booking booking = bookingRepository.findByBookingNo(request.getBookingNo())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约不存在"));

        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能标记已确认的预约为爽约");
        }

        Venue venue = venueRepository.findById(booking.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        LocalDateTime gracePeriodEnd = booking.getBookingDate()
            .atTime(booking.getStartTime())
            .plusMinutes(venue.getNoShowGraceMinutes());

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

        booking.setStatus(4);
        booking.setNoShowMarkedAt(now);
        booking.setNoShowMarkedBy(operatorId);
        bookingRepository.updateById(booking);

        User user = userRepository.findById(booking.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        user.setNoShowCount(user.getNoShowCount() + 1);
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

        return toResponse(record, user);
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

        if (remainingViolations < NO_SHOW_THRESHOLD && user.getStatus() == 0) {
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
            .map(record -> toResponse(record, user))
            .collect(Collectors.toList());
    }

    private ViolationResponse toResponse(ViolationRecord record, User user) {
        ViolationResponse response = new ViolationResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setUserName(user.getName());
        response.setBookingId(record.getBookingId());
        response.setViolationType(record.getViolationType());
        response.setViolationTypeText(record.getViolationType() == 1 ? "爽约" : "超时取消");
        response.setBookingDate(record.getBookingDate());
        response.setMarkedAt(record.getMarkedAt());
        response.setBanDays(record.getBanDays());
        response.setBanUntil(record.getBanUntil());
        response.setClearedAt(record.getClearedAt());

        bookingRepository.findById(record.getBookingId())
            .ifPresent(booking -> response.setBookingNo(booking.getBookingNo()));

        return response;
    }
}
```

---

## Step 4: 创建违约控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/ViolationAdminController.java`

```java
package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.MarkNoShowRequest;
import com.stadium.booking.dto.response.ViolationResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "违约管理", description = "后台违约管理接口")
@RestController
@RequestMapping("/admin/violations")
@RequiredArgsConstructor
public class ViolationAdminController {
    private final ViolationService violationService;

    @Operation(summary = "标记爽约")
    @PostMapping("/no-show")
    @RequirePermission("booking:mark_no_show")
    public Result<ViolationResponse> markNoShow(@Valid @RequestBody MarkNoShowRequest request) {
        return Result.success(violationService.markNoShow(request));
    }

    @Operation(summary = "清除违约记录")
    @DeleteMapping("/{id}")
    @RequirePermission("user:update")
    public Result<Void> clearViolation(@PathVariable Long id) {
        violationService.clearViolation(id);
        return Result.success();
    }

    @Operation(summary = "查询用户违约记录")
    @GetMapping("/user/{userId}")
    @RequirePermission("user:read")
    public Result<List<ViolationResponse>> getUserViolations(@PathVariable Long userId) {
        return Result.success(violationService.getUserViolations(userId));
    }
}
```

---

## Step 5: 验证功能

**测试标记爽约:**
```bash
curl -X POST http://localhost:8080/api/admin/violations/no-show \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"bookingNo": "BK20260213ABCD1234"}'
```

**测试清除违约:**
```bash
curl -X DELETE http://localhost:8080/api/admin/violations/1 \
  -H "Authorization: Bearer <admin_token>"
```

---

## 提交

```bash
git add backend/
git commit -m "feat(violation): implement no-show marking and user ban system"
```

---

## 注意事项

1. **阈值配置**: 建议将阈值配置化，便于不同学校调整
2. **通知用户**: 爽约标记和禁用时应通知用户
3. **定时任务**: 可添加定时任务自动清理过期违约记录
