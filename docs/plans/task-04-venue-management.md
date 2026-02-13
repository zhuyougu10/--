# Task 04: 球馆场地管理

> **依赖:** task-03-auth-module.md
> **预计时间:** 3-4 小时

## 目标
实现球馆和场地的完整 CRUD 管理功能，包括时段生成、可用性查询、场地状态管理等。

---

## 功能概览

```
┌─────────────────────────────────────────────────────────┐
│                  球馆场地管理架构                         │
├─────────────────────────────────────────────────────────┤
│  Campus (校区)                                          │
│     │                                                   │
│     └── Venue (球馆)                                    │
│           │   - 基础信息 (名称、位置、运动类型)           │
│           │   - 营业配置 (开放日、营业时间、时段长度)      │
│           │   - 预约配置 (提前天数、取消截止、限额等)      │
│           │                                              │
│           └── Court (场地)                              │
│                 - 基础信息 (名称、编号)                  │
│                 - 状态管理 (可用/停用/维护中)            │
│                 - 临时关闭                               │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建实体类

**文件:** `backend/src/main/java/com/stadium/booking/entity/Venue.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("venue")
public class Venue extends BaseEntity {
    private Long campusId;
    private String name;
    private String code;
    private String sportType;
    private String location;
    private String description;
    private String imageUrl;
    private String openDays;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotMinutes;
    private Integer bookAheadDays;
    private Integer cancelCutoffMinutes;
    private Integer checkinWindowBefore;
    private Integer noShowGraceMinutes;
    private Integer dailySlotLimit;
    private Integer weeklySlotLimit;
    private Integer groupBookingEnabled;
    private Integer groupMaxCourts;
    private Integer groupMaxHours;
    private Integer status;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/entity/Court.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("court")
public class Court extends BaseEntity {
    private Long venueId;
    private String name;
    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer status;
    private String statusReason;
    private LocalDateTime statusUntil;
    private Integer sortOrder;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/entity/CourtClosure.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("court_closure")
public class CourtClosure extends BaseEntity {
    private Long courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Long createdBy;
}
```

---

## Step 2: 创建 Repository

**文件:** `backend/src/main/java/com/stadium/booking/repository/VenueRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Venue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface VenueRepository extends BaseMapper<Venue> {
    @Select("SELECT * FROM venue WHERE deleted_at IS NULL AND status = 1 ORDER BY id")
    List<Venue> findAllActive();

    @Select("SELECT * FROM venue WHERE id = #{id} AND deleted_at IS NULL")
    Optional<Venue> findById(Long id);

    @Select("SELECT * FROM venue WHERE sport_type = #{sportType} AND deleted_at IS NULL AND status = 1")
    List<Venue> findBySportType(String sportType);

    @Select("SELECT * FROM venue WHERE code = #{code} AND deleted_at IS NULL")
    Optional<Venue> findByCode(String code);
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/CourtRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Court;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CourtRepository extends BaseMapper<Court> {
    @Select("SELECT * FROM court WHERE venue_id = #{venueId} AND deleted_at IS NULL ORDER BY sort_order, id")
    List<Court> findByVenueId(Long venueId);

    @Select("SELECT * FROM court WHERE id = #{id} AND deleted_at IS NULL")
    Optional<Court> findById(Long id);

    @Select("SELECT * FROM court WHERE venue_id = #{venueId} AND status = 1 AND deleted_at IS NULL ORDER BY sort_order, id")
    List<Court> findActiveByVenueId(Long venueId);

    @Select("SELECT COUNT(*) FROM court WHERE venue_id = #{venueId} AND deleted_at IS NULL")
    int countByVenueId(Long venueId);
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/CourtClosureRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.CourtClosure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CourtClosureRepository extends BaseMapper<CourtClosure> {
    @Select("""
        SELECT * FROM court_closure 
        WHERE court_id = #{courtId} 
        AND end_time > #{startTime} 
        AND start_time < #{endTime}
        """)
    List<CourtClosure> findOverlapping(Long courtId, LocalDateTime startTime, LocalDateTime endTime);
}
```

---

## Step 3: 创建 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/VenueCreateRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class VenueCreateRequest {
    @NotBlank(message = "球馆名称不能为空")
    private String name;

    @NotBlank(message = "球馆编码不能为空")
    private String code;

    @NotBlank(message = "运动类型不能为空")
    private String sportType;

    private String location;
    private String description;
    private String imageUrl;

    private String openDays = "1,2,3,4,5,6,7";

    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;

    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;

    private Integer slotMinutes = 60;
    private Integer bookAheadDays = 7;
    private Integer cancelCutoffMinutes = 30;
    private Integer checkinWindowBefore = 15;
    private Integer noShowGraceMinutes = 15;
    private Integer dailySlotLimit = 2;
    private Integer weeklySlotLimit = 10;
    private Integer groupBookingEnabled = 1;
    private Integer groupMaxCourts = 4;
    private Integer groupMaxHours = 4;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/CourtCreateRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtCreateRequest {
    @NotNull(message = "球馆ID不能为空")
    private Long venueId;

    @NotBlank(message = "场地名称不能为空")
    private String name;

    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer sortOrder = 0;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/VenueResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalTime;

@Data
public class VenueResponse {
    private Long id;
    private String name;
    private String code;
    private String sportType;
    private String location;
    private String description;
    private String imageUrl;
    private String openDays;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotMinutes;
    private Integer bookAheadDays;
    private Integer cancelCutoffMinutes;
    private Integer dailySlotLimit;
    private Integer status;
    private Integer courtCount;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/CourtResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourtResponse {
    private Long id;
    private Long venueId;
    private String venueName;
    private String name;
    private String courtNo;
    private String sportType;
    private String floorType;
    private String features;
    private Integer status;
    private String statusReason;
    private LocalDateTime statusUntil;
    private Integer sortOrder;
}
```

---

## Step 4: 创建时段 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/TimeSlotResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotResponse {
    private Long courtId;
    private String courtName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private BookingInfo booking;

    @Data
    public static class BookingInfo {
        private String bookingNo;
        private String userName;
        private String userPhone;
    }
}
```

---

## Step 5: 创建球馆服务

**文件:** `backend/src/main/java/com/stadium/booking/service/VenueService.java`

```java
package com.stadium.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.VenueCreateRequest;
import com.stadium.booking.dto.response.VenueResponse;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;

    public List<VenueResponse> listAll() {
        return venueRepository.findAllActive().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public Page<VenueResponse> listPage(Integer current, Integer size, String sportType, Integer status) {
        LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Venue::getDeletedAt);
        if (sportType != null && !sportType.isEmpty()) {
            wrapper.eq(Venue::getSportType, sportType);
        }
        if (status != null) {
            wrapper.eq(Venue::getStatus, status);
        }
        wrapper.orderByDesc(Venue::getCreatedAt);

        Page<Venue> page = venueRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toResponse);
    }

    public VenueResponse getById(Long id) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        return toResponse(venue);
    }

    @Transactional
    public VenueResponse create(VenueCreateRequest request) {
        if (venueRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "球馆编码已存在");
        }

        Venue venue = new Venue();
        venue.setCampusId(1L);
        venue.setName(request.getName());
        venue.setCode(request.getCode());
        venue.setSportType(request.getSportType());
        venue.setLocation(request.getLocation());
        venue.setDescription(request.getDescription());
        venue.setImageUrl(request.getImageUrl());
        venue.setOpenDays(request.getOpenDays());
        venue.setOpenTime(request.getOpenTime());
        venue.setCloseTime(request.getCloseTime());
        venue.setSlotMinutes(request.getSlotMinutes());
        venue.setBookAheadDays(request.getBookAheadDays());
        venue.setCancelCutoffMinutes(request.getCancelCutoffMinutes());
        venue.setCheckinWindowBefore(request.getCheckinWindowBefore());
        venue.setNoShowGraceMinutes(request.getNoShowGraceMinutes());
        venue.setDailySlotLimit(request.getDailySlotLimit());
        venue.setWeeklySlotLimit(request.getWeeklySlotLimit());
        venue.setGroupBookingEnabled(request.getGroupBookingEnabled());
        venue.setGroupMaxCourts(request.getGroupMaxCourts());
        venue.setGroupMaxHours(request.getGroupMaxHours());
        venue.setStatus(1);

        venueRepository.insert(venue);
        return toResponse(venue);
    }

    @Transactional
    public VenueResponse update(Long id, VenueCreateRequest request) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        venue.setName(request.getName());
        venue.setSportType(request.getSportType());
        venue.setLocation(request.getLocation());
        venue.setDescription(request.getDescription());
        venue.setImageUrl(request.getImageUrl());
        venue.setOpenDays(request.getOpenDays());
        venue.setOpenTime(request.getOpenTime());
        venue.setCloseTime(request.getCloseTime());
        venue.setSlotMinutes(request.getSlotMinutes());
        venue.setBookAheadDays(request.getBookAheadDays());
        venue.setCancelCutoffMinutes(request.getCancelCutoffMinutes());
        venue.setCheckinWindowBefore(request.getCheckinWindowBefore());
        venue.setNoShowGraceMinutes(request.getNoShowGraceMinutes());
        venue.setDailySlotLimit(request.getDailySlotLimit());
        venue.setWeeklySlotLimit(request.getWeeklySlotLimit());
        venue.setGroupBookingEnabled(request.getGroupBookingEnabled());
        venue.setGroupMaxCourts(request.getGroupMaxCourts());
        venue.setGroupMaxHours(request.getGroupMaxHours());

        venueRepository.updateById(venue);
        return toResponse(venue);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        venue.setStatus(status);
        venueRepository.updateById(venue);
    }

    @Transactional
    public void delete(Long id) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        venueRepository.deleteById(id);
    }

    private VenueResponse toResponse(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setId(venue.getId());
        response.setName(venue.getName());
        response.setCode(venue.getCode());
        response.setSportType(venue.getSportType());
        response.setLocation(venue.getLocation());
        response.setDescription(venue.getDescription());
        response.setImageUrl(venue.getImageUrl());
        response.setOpenDays(venue.getOpenDays());
        response.setOpenTime(venue.getOpenTime());
        response.setCloseTime(venue.getCloseTime());
        response.setSlotMinutes(venue.getSlotMinutes());
        response.setBookAheadDays(venue.getBookAheadDays());
        response.setCancelCutoffMinutes(venue.getCancelCutoffMinutes());
        response.setDailySlotLimit(venue.getDailySlotLimit());
        response.setStatus(venue.getStatus());
        response.setCourtCount(courtRepository.countByVenueId(venue.getId()));
        return response;
    }
}
```

---

## Step 6: 创建场地服务

**文件:** `backend/src/main/java/com/stadium/booking/service/CourtService.java`

```java
package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.CourtCreateRequest;
import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtService {
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    public List<CourtResponse> listByVenue(Long venueId) {
        return courtRepository.findByVenueId(venueId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public CourtResponse getById(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        return toResponse(court);
    }

    @Transactional
    public CourtResponse create(CourtCreateRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        Court court = new Court();
        court.setVenueId(request.getVenueId());
        court.setName(request.getName());
        court.setCourtNo(request.getCourtNo());
        court.setSportType(request.getSportType() != null ? request.getSportType() : venue.getSportType());
        court.setFloorType(request.getFloorType());
        court.setFeatures(request.getFeatures());
        court.setSortOrder(request.getSortOrder());
        court.setStatus(1);

        courtRepository.insert(court);
        return toResponse(court);
    }

    @Transactional
    public CourtResponse update(Long id, CourtCreateRequest request) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));

        court.setName(request.getName());
        court.setCourtNo(request.getCourtNo());
        court.setSportType(request.getSportType());
        court.setFloorType(request.getFloorType());
        court.setFeatures(request.getFeatures());
        court.setSortOrder(request.getSortOrder());

        courtRepository.updateById(court);
        return toResponse(court);
    }

    @Transactional
    public void updateStatus(Long id, Integer status, String reason) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        court.setStatus(status);
        court.setStatusReason(reason);
        courtRepository.updateById(court);
    }

    @Transactional
    public void delete(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        courtRepository.deleteById(id);
    }

    private CourtResponse toResponse(Court court) {
        CourtResponse response = new CourtResponse();
        response.setId(court.getId());
        response.setVenueId(court.getVenueId());
        response.setName(court.getName());
        response.setCourtNo(court.getCourtNo());
        response.setSportType(court.getSportType());
        response.setFloorType(court.getFloorType());
        response.setFeatures(court.getFeatures());
        response.setStatus(court.getStatus());
        response.setStatusReason(court.getStatusReason());
        response.setStatusUntil(court.getStatusUntil());
        response.setSortOrder(court.getSortOrder());

        venueRepository.findById(court.getVenueId())
            .ifPresent(venue -> response.setVenueName(venue.getName()));

        return response;
    }
}
```

---

## Step 7: 创建时段服务

**文件:** `backend/src/main/java/com/stadium/booking/service/TimeSlotService.java`

```java
package com.stadium.booking.service;

import com.stadium.booking.dto.response.TimeSlotResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.CourtClosure;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtClosureRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final CourtClosureRepository courtClosureRepository;

    public List<TimeSlotResponse> getAvailableSlots(Long venueId, Long courtId, LocalDate date) {
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new RuntimeException("球馆不存在"));

        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("场地不存在"));

        List<TimeSlotResponse> slots = generateTimeSlots(venue, court, date);

        List<Booking> bookings = bookingRepository.findByCourtIdAndDate(courtId, date);
        Map<String, Booking> bookingMap = bookings.stream()
            .collect(Collectors.toMap(
                b -> b.getStartTime().toString(),
                b -> b,
                (a, b) -> a
            ));

        LocalDateTime slotStart = date.atTime(venue.getOpenTime());
        LocalDateTime slotEnd = date.atTime(venue.getCloseTime());
        List<CourtClosure> closures = courtClosureRepository.findOverlapping(courtId, slotStart, slotEnd);

        boolean isAdmin = UserContext.isCurrentUserAdmin();

        for (TimeSlotResponse slot : slots) {
            String key = slot.getStartTime().toString();
            if (bookingMap.containsKey(key)) {
                Booking booking = bookingMap.get(key);
                slot.setStatus("occupied");
                if (isAdmin) {
                    TimeSlotResponse.BookingInfo info = new TimeSlotResponse.BookingInfo();
                    info.setBookingNo(booking.getBookingNo());
                    info.setUserName(booking.getUserName());
                    info.setUserPhone(booking.getUserPhone());
                    slot.setBooking(info);
                }
            } else if (isCourtClosed(closures, slot)) {
                slot.setStatus("closed");
            } else {
                slot.setStatus("free");
            }
        }

        return slots;
    }

    private List<TimeSlotResponse> generateTimeSlots(Venue venue, Court court, LocalDate date) {
        List<TimeSlotResponse> slots = new ArrayList<>();

        LocalTime currentTime = venue.getOpenTime();
        LocalTime closeTime = venue.getCloseTime();
        int slotMinutes = venue.getSlotMinutes();

        while (currentTime.isBefore(closeTime)) {
            LocalTime endTime = currentTime.plusMinutes(slotMinutes);
            if (endTime.isAfter(closeTime)) {
                break;
            }

            TimeSlotResponse slot = new TimeSlotResponse();
            slot.setCourtId(court.getId());
            slot.setCourtName(court.getName());
            slot.setDate(date);
            slot.setStartTime(currentTime);
            slot.setEndTime(endTime);
            slot.setStatus("free");

            slots.add(slot);
            currentTime = endTime;
        }

        return slots;
    }

    private boolean isCourtClosed(List<CourtClosure> closures, TimeSlotResponse slot) {
        LocalDateTime slotStart = slot.getDate().atTime(slot.getStartTime());
        LocalDateTime slotEnd = slot.getDate().atTime(slot.getEndTime());

        return closures.stream().anyMatch(closure ->
            closure.getStartTime().isBefore(slotEnd) && closure.getEndTime().isAfter(slotStart)
        );
    }
}
```

---

## Step 8: 创建控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/VenueApiController.java`

```java
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
@RequestMapping("/api/venues")
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
```

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/VenueAdminController.java`

```java
package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.VenueCreateRequest;
import com.stadium.booking.dto.response.VenueResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "球馆管理", description = "后台球馆管理接口")
@RestController
@RequestMapping("/admin/venues")
@RequiredArgsConstructor
public class VenueAdminController {
    private final VenueService venueService;

    @Operation(summary = "分页查询球馆")
    @GetMapping
    @RequirePermission("venue:read")
    public Result<Page<VenueResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) Integer status) {
        return Result.success(venueService.listPage(current, size, sportType, status));
    }

    @Operation(summary = "获取球馆详情")
    @GetMapping("/{id}")
    @RequirePermission("venue:read")
    public Result<VenueResponse> getById(@PathVariable Long id) {
        return Result.success(venueService.getById(id));
    }

    @Operation(summary = "创建球馆")
    @PostMapping
    @RequirePermission("venue:create")
    public Result<VenueResponse> create(@Valid @RequestBody VenueCreateRequest request) {
        return Result.success(venueService.create(request));
    }

    @Operation(summary = "更新球馆")
    @PutMapping("/{id}")
    @RequirePermission("venue:update")
    public Result<VenueResponse> update(@PathVariable Long id, @Valid @RequestBody VenueCreateRequest request) {
        return Result.success(venueService.update(id, request));
    }

    @Operation(summary = "更新球馆状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("venue:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        venueService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除球馆")
    @DeleteMapping("/{id}")
    @RequirePermission("venue:delete")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return Result.success();
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/CourtAdminController.java`

```java
package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.CourtCreateRequest;
import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "场地管理", description = "后台场地管理接口")
@RestController
@RequestMapping("/admin/courts")
@RequiredArgsConstructor
public class CourtAdminController {
    private final CourtService courtService;

    @Operation(summary = "获取球馆下的场地列表")
    @GetMapping("/venue/{venueId}")
    @RequirePermission("court:read")
    public Result<List<CourtResponse>> listByVenue(@PathVariable Long venueId) {
        return Result.success(courtService.listByVenue(venueId));
    }

    @Operation(summary = "获取场地详情")
    @GetMapping("/{id}")
    @RequirePermission("court:read")
    public Result<CourtResponse> getById(@PathVariable Long id) {
        return Result.success(courtService.getById(id));
    }

    @Operation(summary = "创建场地")
    @PostMapping
    @RequirePermission("court:create")
    public Result<CourtResponse> create(@Valid @RequestBody CourtCreateRequest request) {
        return Result.success(courtService.create(request));
    }

    @Operation(summary = "更新场地")
    @PutMapping("/{id}")
    @RequirePermission("court:update")
    public Result<CourtResponse> update(@PathVariable Long id, @Valid @RequestBody CourtCreateRequest request) {
        return Result.success(courtService.update(id, request));
    }

    @Operation(summary = "更新场地状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("court:update")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        courtService.updateStatus(id, status, reason);
        return Result.success();
    }

    @Operation(summary = "删除场地")
    @DeleteMapping("/{id}")
    @RequirePermission("court:delete")
    public Result<Void> delete(@PathVariable Long id) {
        courtService.delete(id);
        return Result.success();
    }
}
```

---

## Step 9: 添加 BookingRepository 方法

**文件:** `backend/src/main/java/com/stadium/booking/repository/BookingRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

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
}
```

---

## Step 10: 验证功能

**测试获取球馆列表:**
```bash
curl http://localhost:8080/api/api/venues
```

**测试创建球馆:**
```bash
curl -X POST http://localhost:8080/api/admin/venues \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "羽毛球馆",
    "code": "BADMINTON_01",
    "sportType": "badminton",
    "location": "体育馆一楼",
    "openTime": "08:00",
    "closeTime": "22:00"
  }'
```

**测试获取时段:**
```bash
curl "http://localhost:8080/api/api/venues/1/courts/1/slots?date=2026-02-15"
```

---

## 提交

```bash
git add backend/
git commit -m "feat(venue): implement venue and court management with time slots"
```
