# Task 07: 二维码核销

> **依赖:** task-05-booking-core.md
> **预计时间:** 3-4 小时

## 目标
实现二维码核销签到功能，包括生成核销二维码、扫码核销、核销幂等性保证。

---

## 核销流程概览

```
┌─────────────────────────────────────────────────────────┐
│                    二维码核销流程                        │
├─────────────────────────────────────────────────────────┤
│  小程序端                                                │
│     │                                                   │
│     ▼                                                   │
│  获取核销码 (生成短时效 QR Token)                        │
│     │                                                   │
│     ▼                                                   │
│  展示二维码                                              │
│     │                                                   │
│     │  扫码                                             │
│     ▼                                                   │
│  后台端 (场馆员)                                         │
│     │                                                   │
│     ▼                                                   │
│  ┌─────────────────────┐                               │
│  │   核销校验           │                               │
│  │  - Token有效性       │                               │
│  │  - 预约状态          │                               │
│  │  - 核销窗口          │                               │
│  └─────────┬───────────┘                               │
│            │ 通过                                       │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │   核销记录           │  (幂等保证)                   │
│  │  - 状态变更          │                               │
│  │  - 记录核销人/时间   │                               │
│  │  - 写入审计日志      │                               │
│  └─────────────────────┘                               │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建 QR Token 实体和 Repository

**文件:** `backend/src/main/java/com/stadium/booking/entity/QrToken.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("qr_token")
public class QrToken {
    private Long id;
    private String token;
    private Long bookingId;
    private Long userId;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/QrTokenRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.QrToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface QrTokenRepository extends BaseMapper<QrToken> {
    @Select("SELECT * FROM qr_token WHERE token = #{token}")
    Optional<QrToken> findByToken(String token);

    @Select("SELECT * FROM qr_token WHERE booking_id = #{bookingId} AND expires_at > NOW() AND used_at IS NULL ORDER BY id DESC LIMIT 1")
    Optional<QrToken> findActiveByBookingId(Long bookingId);

    @Update("UPDATE qr_token SET used_at = #{usedAt} WHERE id = #{id} AND used_at IS NULL")
    int markAsUsed(@Param("id") Long id, @Param("usedAt") LocalDateTime usedAt);

    @Update("DELETE FROM qr_token WHERE expires_at < #{cutoff}")
    int deleteExpired(LocalDateTime cutoff);
}
```

---

## Step 2: 创建核销记录实体和 Repository

**文件:** `backend/src/main/java/com/stadium/booking/entity/CheckinRecord.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {
    private Long id;
    private Long bookingId;
    private String bookingNo;
    private String qrToken;
    private Integer checkinMethod;
    private Long checkedInBy;
    private LocalDateTime checkedInAt;
    private Long venueId;
    private LocalDateTime createdAt;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/CheckinRecordRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface CheckinRecordRepository extends BaseMapper<CheckinRecord> {
    @Select("SELECT * FROM checkin_record WHERE booking_id = #{bookingId}")
    Optional<CheckinRecord> findByBookingId(Long bookingId);
}
```

---

## Step 3: 创建核销 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/CheckinRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckinRequest {
    @NotBlank(message = "token不能为空")
    private String token;

    private Integer checkinMethod = 1;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/ManualCheckinRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualCheckinRequest {
    @NotBlank(message = "预约单号不能为空")
    private String bookingNo;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/CheckinResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CheckinResponse {
    private Boolean success;
    private String message;
    private String bookingNo;
    private String venueName;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String userName;
    private LocalDateTime checkedInAt;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/QrCodeResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QrCodeResponse {
    private String token;
    private String qrData;
    private LocalDateTime expiresAt;
}
```

---

## Step 4: 创建核销服务

**文件:** `backend/src/main/java/com/stadium/booking/service/CheckinService.java`

```java
package com.stadium.booking.service;

import cn.hutool.core.util.IdUtil;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.CheckinRequest;
import com.stadium.booking.dto.request.ManualCheckinRequest;
import com.stadium.booking.dto.response.CheckinResponse;
import com.stadium.booking.dto.response.QrCodeResponse;
import com.stadium.booking.entity.*;
import com.stadium.booking.repository.*;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
            qrTokenRepository.markAsUsed(
                qrTokenRepository.findByToken(qrToken).map(QrToken::getId).orElse(null),
                now
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
```

---

## Step 5: 创建核销控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/QrCodeApiController.java`

```java
package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.QrCodeResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "二维码API", description = "小程序端二维码接口")
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeApiController {
    private final CheckinService checkinService;

    @Operation(summary = "获取核销二维码")
    @GetMapping("/booking/{bookingNo}")
    public Result<QrCodeResponse> getQrCode(@PathVariable String bookingNo) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(checkinService.generateQrToken(userId, bookingNo));
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/CheckinAdminController.java`

```java
package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.CheckinRequest;
import com.stadium.booking.dto.request.ManualCheckinRequest;
import com.stadium.booking.dto.response.CheckinResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "核销管理", description = "后台核销接口")
@RestController
@RequestMapping("/admin/checkin")
@RequiredArgsConstructor
public class CheckinAdminController {
    private final CheckinService checkinService;

    @Operation(summary = "扫码核销")
    @PostMapping("/scan")
    @RequirePermission("booking:checkin")
    public Result<CheckinResponse> scanCheckin(@Valid @RequestBody CheckinRequest request) {
        return Result.success(checkinService.checkin(request));
    }

    @Operation(summary = "手动核销")
    @PostMapping("/manual")
    @RequirePermission("booking:checkin")
    public Result<CheckinResponse> manualCheckin(@Valid @RequestBody ManualCheckinRequest request) {
        return Result.success(checkinService.manualCheckin(request));
    }
}
```

---

## Step 6: 验证功能

**测试获取核销码:**
```bash
curl http://localhost:8080/api/api/qrcode/booking/BK20260213ABCD1234 \
  -H "Authorization: Bearer <token>"
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "QRABC123DEF456GHI789",
    "qrData": "QRABC123DEF456GHI789",
    "expiresAt": "2026-02-13T10:35:00"
  }
}
```

**测试扫码核销:**
```bash
curl -X POST http://localhost:8080/api/admin/checkin/scan \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"token": "QRABC123DEF456GHI789"}'
```

---

## 提交

```bash
git add backend/
git commit -m "feat(checkin): implement QR code checkin with idempotency"
```

---

## 注意事项

1. **幂等性**: 使用唯一索引和状态检查保证核销幂等
2. **Token安全**: Token有效期短(5分钟)，使用后立即失效
3. **审计追踪**: 所有核销操作必须记录审计日志
