# Task 06: 智能推荐系统

> **依赖:** task-05-booking-core.md
> **预计时间:** 3-4 小时

## 目标
实现智能推荐功能，当用户选择的时段不可用时，自动推荐可行的替代方案，提升预约成功率。

---

## 推荐策略概览

```
┌─────────────────────────────────────────────────────────┐
│                    推荐算法流程                          │
├─────────────────────────────────────────────────────────┤
│  用户输入: 球馆/运动类型 + 日期 + 期望时段               │
│           │                                             │
│           ▼                                             │
│  ┌─────────────────────┐                               │
│  │  同球馆同场地优先    │  时间偏移 ±K 时段             │
│  └─────────┬───────────┘                               │
│            │ 无可用                                     │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │  同球馆不同场地      │  场地编号相近优先             │
│  └─────────┬───────────┘                               │
│            │ 无可用                                     │
│            ▼                                             │
│  ┌─────────────────────┐                               │
│  │  替代球馆            │  同运动类型其他球馆           │
│  └─────────┬───────────┘                               │
│            │                                             │
│            ▼                                             │
│  返回 Top N 推荐方案 (带推荐理由)                        │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建推荐请求/响应 DTO

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/RecommendationRequest.java`

```java
package com.stadium.booking.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RecommendationRequest {
    private Long venueId;
    private String sportType;
    
    private LocalDate date;
    private LocalTime preferredStartTime;
    private Integer durationMinutes = 60;
    
    private Integer maxResults = 10;
    private Integer timeOffsetSlots = 2;
    private Boolean allowAlternativeVenue = true;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/RecommendationResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class RecommendationResponse {
    private List<RecommendationItem> recommendations;
    private Integer totalCount;

    @Data
    public static class RecommendationItem {
        private Long venueId;
        private String venueName;
        private Long courtId;
        private String courtName;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String label;
        private String reason;
        private Integer score;
    }
}
```

---

## Step 2: 创建推荐服务

**文件:** `backend/src/main/java/com/stadium/booking/service/RecommendationService.java`

```java
package com.stadium.booking.service;

import com.stadium.booking.dto.request.RecommendationRequest;
import com.stadium.booking.dto.response.RecommendationResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        if (request.getVenueId() != null) {
            items.addAll(findInVenue(request, request.getVenueId()));
        }

        if (items.size() < request.getMaxResults() && 
            Boolean.TRUE.equals(request.getAllowAlternativeVenue()) &&
            request.getSportType() != null) {
            items.addAll(findAlternativeVenues(request));
        }

        items.sort((a, b) -> b.getScore().compareTo(a.getScore()));

        items = items.stream()
            .limit(request.getMaxResults())
            .collect(Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(items);
        response.setTotalCount(items.size());
        return response;
    }

    private List<RecommendationResponse.RecommendationItem> findInVenue(
            RecommendationRequest request, Long venueId) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        Venue venue = venueRepository.findById(venueId).orElse(null);
        if (venue == null || venue.getStatus() != 1) {
            return items;
        }

        List<Court> courts = courtRepository.findActiveByVenueId(venueId);
        int slotMinutes = venue.getSlotMinutes();
        LocalTime preferredStart = request.getPreferredStartTime();
        if (preferredStart == null) {
            preferredStart = venue.getOpenTime();
        }

        for (Court court : courts) {
            items.addAll(findAvailableSlots(venue, court, request.getDate(), 
                preferredStart, request.getDurationMinutes(), slotMinutes, 
                request.getTimeOffsetSlots()));
        }

        return items;
    }

    private List<RecommendationResponse.RecommendationItem> findAvailableSlots(
            Venue venue, Court court, LocalDate date, LocalTime preferredStart,
            int durationMinutes, int slotMinutes, int timeOffsetSlots) {
        
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();
        
        LocalTime searchStart = preferredStart.minusMinutes(timeOffsetSlots * slotMinutes);
        if (searchStart.isBefore(venue.getOpenTime())) {
            searchStart = venue.getOpenTime();
        }

        LocalTime searchEnd = preferredStart.plusMinutes((timeOffsetSlots + 1) * slotMinutes + durationMinutes);
        if (searchEnd.isAfter(venue.getCloseTime())) {
            searchEnd = venue.getCloseTime();
        }

        List<Booking> bookings = bookingRepository.findByCourtIdAndDate(court.getId(), date);
        Set<String> occupiedSlots = bookings.stream()
            .filter(b -> b.getStatus() == 1)
            .map(b -> b.getStartTime().toString() + "-" + b.getEndTime().toString())
            .collect(Collectors.toSet());

        LocalTime currentStart = alignToSlot(searchStart, slotMinutes);
        while (currentStart.plusMinutes(durationMinutes).compareTo(searchEnd) <= 0) {
            LocalTime end = currentStart.plusMinutes(durationMinutes);
            
            if (end.compareTo(venue.getCloseTime()) <= 0 && 
                isSlotAvailable(currentStart, end, occupiedSlots, slotMinutes)) {
                
                RecommendationResponse.RecommendationItem item = createItem(
                    venue, court, date, currentStart, end, preferredStart, slotMinutes);
                items.add(item);
            }
            
            currentStart = currentStart.plusMinutes(slotMinutes);
        }

        return items;
    }

    private boolean isSlotAvailable(LocalTime start, LocalTime end, 
                                   Set<String> occupiedSlots, int slotMinutes) {
        LocalTime current = start;
        while (current.compareTo(end) < 0) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            String slotKey = current.toString() + "-" + slotEnd.toString();
            if (occupiedSlots.contains(slotKey)) {
                return false;
            }
            current = slotEnd;
        }
        return true;
    }

    private LocalTime alignToSlot(LocalTime time, int slotMinutes) {
        int minutes = time.getMinute();
        int alignedMinutes = (minutes / slotMinutes) * slotMinutes;
        return time.withMinute(alignedMinutes).withSecond(0).withNano(0);
    }

    private RecommendationResponse.RecommendationItem createItem(
            Venue venue, Court court, LocalDate date, LocalTime start, LocalTime end,
            LocalTime preferredStart, int slotMinutes) {
        
        RecommendationResponse.RecommendationItem item = new RecommendationResponse.RecommendationItem();
        item.setVenueId(venue.getId());
        item.setVenueName(venue.getName());
        item.setCourtId(court.getId());
        item.setCourtName(court.getName());
        item.setDate(date);
        item.setStartTime(start);
        item.setEndTime(end);

        int timeDiff = (int) Math.abs(java.time.Duration.between(preferredStart, start).toMinutes());
        int slotDiff = timeDiff / slotMinutes;

        if (slotDiff == 0) {
            item.setLabel("期望时间");
            item.setReason("完全匹配您的期望时间");
            item.setScore(100);
        } else if (start.isBefore(preferredStart)) {
            item.setLabel("提前" + slotDiff + "时段");
            item.setReason("比期望时间提前" + slotDiff + "个时段");
            item.setScore(90 - slotDiff * 5);
        } else {
            item.setLabel("延后" + slotDiff + "时段");
            item.setReason("比期望时间延后" + slotDiff + "个时段");
            item.setScore(85 - slotDiff * 5);
        }

        return item;
    }

    private List<RecommendationResponse.RecommendationItem> findAlternativeVenues(
            RecommendationRequest request) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        List<Venue> alternativeVenues = venueRepository.findBySportType(request.getSportType());
        
        for (Venue venue : alternativeVenues) {
            if (request.getVenueId() != null && venue.getId().equals(request.getVenueId())) {
                continue;
            }

            List<RecommendationResponse.RecommendationItem> venueItems = findInVenue(request, venue.getId());
            
            for (RecommendationResponse.RecommendationItem item : venueItems) {
                item.setLabel("替代球馆");
                item.setReason("同运动类型其他球馆: " + venue.getName());
                item.setScore(item.getScore() - 20);
            }
            
            items.addAll(venueItems);
        }

        return items;
    }
}
```

---

## Step 3: 创建推荐控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/RecommendationApiController.java`

```java
package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.RecommendationRequest;
import com.stadium.booking.dto.response.RecommendationResponse;
import com.stadium.booking.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能推荐API", description = "预约推荐接口")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationApiController {
    private final RecommendationService recommendationService;

    @Operation(summary = "获取推荐方案")
    @PostMapping
    public Result<RecommendationResponse> getRecommendations(
            @RequestBody RecommendationRequest request) {
        return Result.success(recommendationService.getRecommendations(request));
    }
}
```

---

## Step 4: 验证功能

**测试推荐接口:**
```bash
curl -X POST http://localhost:8080/api/api/recommendations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "date": "2026-02-15",
    "preferredStartTime": "10:00",
    "durationMinutes": 60,
    "maxResults": 5
  }'
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "venueId": 1,
        "venueName": "羽毛球馆",
        "courtId": 1,
        "courtName": "1号场",
        "date": "2026-02-15",
        "startTime": "10:00",
        "endTime": "11:00",
        "label": "期望时间",
        "reason": "完全匹配您的期望时间",
        "score": 100
      }
    ],
    "totalCount": 1
  }
}
```

---

## 提交

```bash
git add backend/
git commit -m "feat(recommendation): implement smart booking recommendation system"
```

---

## 注意事项

1. **性能优化**: 对于大量场地，建议缓存可用性数据
2. **推荐排序**: 可根据历史数据调整评分权重
3. **扩展性**: 预留接口支持更复杂的推荐算法
