# 前后端API对接修复计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修复前端admin-web与后端API接口对接问题，确保所有前端调用的接口在后端都有对应实现

**Architecture:** 后端采用Spring Boot + MyBatis-Plus，前端采用Vue3 + Ant Design Vue。需要补充缺失的后端Controller接口，调整路径不匹配的接口。

**Tech Stack:** Java 17, Spring Boot 3.x, MyBatis-Plus, Vue3, Axios

---

## 问题汇总

| 模块 | 问题类型 | 数量 |
|------|----------|------|
| 认证模块 | 后端缺失接口 | 2个 |
| 预约模块 | 缺失+路径不匹配 | 2个 |
| 场地模块 | 缺失+路径不匹配 | 2个 |
| 用户模块 | 整个模块缺失 | 5个 |
| 签到模块 | 后端缺失接口 | 1个 |

---

## Task 1: 补充认证模块缺失接口

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/AdminAuthController.java`

**Step 1: 添加获取当前用户信息接口**

在 `AdminAuthController.java` 中添加：

```java
@Operation(summary = "获取当前管理员信息")
@GetMapping("/profile")
public Result<LoginResponse> getProfile() {
    Long adminId = UserContext.getCurrentUserId();
    AdminUser admin = adminUserRepository.findById(adminId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    
    LoginResponse response = new LoginResponse();
    response.setUserId(admin.getId());
    response.setUserType("ADMIN");
    return Result.success(response);
}
```

**Step 2: 添加登出接口**

```java
@Operation(summary = "管理员登出")
@PostMapping("/logout")
public Result<Void> logout() {
    return Result.success();
}
```

**Step 3: 添加必要的导入**

确保文件顶部有：
```java
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.security.UserContext;
```

**Step 4: 注入AdminUserRepository**

在类中添加：
```java
private final AdminUserRepository adminUserRepository;
```

**Step 5: 验证**

启动后端服务，测试：
```bash
curl -X GET http://localhost:8080/api/admin/auth/profile -H "Authorization: Bearer <token>"
curl -X POST http://localhost:8080/api/admin/auth/logout -H "Authorization: Bearer <token>"
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/admin/AdminAuthController.java
git commit -m "feat: add profile and logout endpoints to AdminAuthController"
```

---

## Task 2: 补充预约模块接口

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/BookingAdminController.java`

**Step 1: 添加今日预约接口**

```java
@Operation(summary = "获取今日预约")
@GetMapping("/today")
@RequirePermission("booking:read")
public Result<List<BookingResponse>> getTodayBookings(
        @RequestParam(required = false) Long venueId) {
    return Result.success(bookingService.getTodayBookings(venueId));
}
```

**Step 2: 添加按bookingNo查询接口**

修改现有的 `getById` 方法，添加按bookingNo查询：

```java
@Operation(summary = "获取预约详情")
@GetMapping("/{bookingNoOrId}")
@RequirePermission("booking:read")
public Result<BookingResponse> getById(@PathVariable String bookingNoOrId) {
    if (bookingNoOrId.startsWith("BK")) {
        return Result.success(bookingService.getBookingByNo(bookingNoOrId));
    }
    return Result.success(bookingService.getBookingById(Long.parseLong(bookingNoOrId)));
}
```

**Step 3: 在BookingService中添加getTodayBookings方法**

在 `BookingService.java` 中添加：

```java
public List<BookingResponse> getTodayBookings(Long venueId) {
    LocalDate today = LocalDate.now();
    if (venueId != null) {
        return getVenueBookings(venueId, today);
    }
    LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
    wrapper.isNull(Booking::getDeletedAt)
           .eq(Booking::getBookingDate, today)
           .orderByAsc(Booking::getStartTime);
    return bookingRepository.selectList(wrapper).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
}
```

**Step 4: 验证**

```bash
curl "http://localhost:8080/api/admin/bookings/today?venueId=1" -H "Authorization: Bearer <token>"
curl "http://localhost:8080/api/admin/bookings/BK202602140001" -H "Authorization: Bearer <token>"
```

**Step 5: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/admin/BookingAdminController.java
git add backend/src/main/java/com/stadium/booking/service/BookingService.java
git commit -m "feat: add today bookings endpoint and bookingNo query support"
```

---

## Task 3: 修复场地模块接口

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/CourtAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/CourtService.java`

**Step 1: 添加场地分页列表接口**

在 `CourtAdminController.java` 中添加：

```java
@Operation(summary = "分页查询场地")
@GetMapping
@RequirePermission("court:read")
public Result<IPage<CourtResponse>> list(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) Long venueId,
        @RequestParam(required = false) Integer status) {
    return Result.success(courtService.listPage(current, size, venueId, status));
}
```

**Step 2: 修改场馆下场地列表路径**

前端调用: `GET /admin/venues/:venueId/courts`
后端现有: `GET /admin/courts/venue/:venueId`

添加兼容路径：

```java
@Operation(summary = "获取球馆下的场地列表")
@GetMapping({"/venue/{venueId}", "/by-venue/{venueId}"})
@RequirePermission("court:read")
public Result<List<CourtResponse>> listByVenue(@PathVariable Long venueId) {
    return Result.success(courtService.listByVenue(venueId));
}
```

**Step 3: 在CourtService中添加分页查询方法**

```java
public IPage<CourtResponse> listPage(Integer current, Integer size, Long venueId, Integer status) {
    LambdaQueryWrapper<Court> wrapper = new LambdaQueryWrapper<>();
    wrapper.isNull(Court::getDeletedAt);
    
    if (venueId != null) {
        wrapper.eq(Court::getVenueId, venueId);
    }
    if (status != null) {
        wrapper.eq(Court::getStatus, status);
    }
    wrapper.orderByAsc(Court::getSortOrder);
    
    IPage<Court> page = courtRepository.selectPage(new Page<>(current, size), wrapper);
    return page.convert(this::toResponse);
}
```

**Step 4: 添加必要的导入**

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

**Step 5: 验证**

```bash
curl "http://localhost:8080/api/admin/courts?current=1&size=10" -H "Authorization: Bearer <token>"
curl "http://localhost:8080/api/admin/courts/venue/1" -H "Authorization: Bearer <token>"
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/admin/CourtAdminController.java
git add backend/src/main/java/com/stadium/booking/service/CourtService.java
git commit -m "feat: add court pagination list and fix venue courts path"
```

---

## Task 4: 创建用户管理模块

**Files:**
- Create: `backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/UserService.java` (如不存在则创建)
- Modify: `backend/src/main/java/com/stadium/booking/repository/UserRepository.java`

**Step 1: 创建UserAdminController**

```java
package com.stadium.booking.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.dto.response.ViolationResponse;
import com.stadium.booking.security.RequirePermission;
import com.stadium.booking.service.BookingService;
import com.stadium.booking.service.ViolationService;
import com.stadium.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "用户管理", description = "后台用户管理接口")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;
    private final BookingService bookingService;
    private final ViolationService violationService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    @RequirePermission("user:read")
    public Result<IPage<UserResponse>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.listPage(current, size, keyword, status));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @RequirePermission("user:read")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    @Operation(summary = "获取用户预约记录")
    @GetMapping("/{id}/bookings")
    @RequirePermission("user:read")
    public Result<List<BookingResponse>> getUserBookings(@PathVariable Long id) {
        return Result.success(bookingService.getUserBookings(id));
    }

    @Operation(summary = "获取用户违约记录")
    @GetMapping("/{id}/violations")
    @RequirePermission("user:read")
    public Result<List<ViolationResponse>> getUserViolations(@PathVariable Long id) {
        return Result.success(violationService.getUserViolations(id));
    }

    @Operation(summary = "更新用户状态")
    @PatchMapping("/{id}/status")
    @RequirePermission("user:update")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}
```

**Step 2: 创建UserResponse DTO**

在 `dto/response/UserResponse.java`:

```java
package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private Integer userType;
    private String userTypeText;
    private Integer status;
    private String statusText;
    private Integer noShowCount;
    private LocalDateTime lastNoShowAt;
    private LocalDateTime bannedUntil;
    private LocalDateTime createdAt;
}
```

**Step 3: 创建UserService**

```java
package com.stadium.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.entity.User;
import com.stadium.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public IPage<UserResponse> listPage(Integer current, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(User::getDeletedAt);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(User::getName, keyword)
                .or()
                .like(User::getPhone, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> page = userRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toResponse);
    }

    public UserResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        return toResponse(user);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setStatus(status);
        if (status == 1) {
            user.setBannedUntil(null);
        }
        userRepository.updateById(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setUserType(user.getUserType());
        response.setUserTypeText(getUserTypeText(user.getUserType()));
        response.setStatus(user.getStatus());
        response.setStatusText(getStatusText(user.getStatus()));
        response.setNoShowCount(user.getNoShowCount());
        response.setLastNoShowAt(user.getLastNoShowAt());
        response.setBannedUntil(user.getBannedUntil());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private String getUserTypeText(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "普通用户";
            case 2 -> "会员";
            default -> "未知";
        };
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "禁用";
            case 1 -> "正常";
            default -> "未知";
        };
    }
}
```

**Step 4: 扩展UserRepository**

```java
@Select("SELECT * FROM user WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) AND deleted_at IS NULL")
List<User> findByKeyword(String keyword);
```

**Step 5: 验证**

```bash
curl "http://localhost:8080/api/admin/users?current=1&size=10" -H "Authorization: Bearer <token>"
curl "http://localhost:8080/api/admin/users/1" -H "Authorization: Bearer <token>"
curl "http://localhost:8080/api/admin/users/1/bookings" -H "Authorization: Bearer <token>"
curl "http://localhost:8080/api/admin/users/1/violations" -H "Authorization: Bearer <token>"
curl -X PATCH "http://localhost:8080/api/admin/users/1/status?status=0" -H "Authorization: Bearer <token>"
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java
git add backend/src/main/java/com/stadium/booking/dto/response/UserResponse.java
git add backend/src/main/java/com/stadium/booking/service/UserService.java
git add backend/src/main/java/com/stadium/booking/repository/UserRepository.java
git commit -m "feat: add user management module for admin"
```

---

## Task 5: 补充签到记录列表接口

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/CheckinAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/CheckinService.java`
- Modify: `backend/src/main/java/com/stadium/booking/repository/CheckinRecordRepository.java`

**Step 1: 添加签到记录列表接口**

在 `CheckinAdminController.java` 中添加：

```java
@Operation(summary = "获取签到记录列表")
@GetMapping("/records")
@RequirePermission("booking:read")
public Result<IPage<CheckinResponse>> getRecords(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) Long venueId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return Result.success(checkinService.listRecords(current, size, venueId, date));
}
```

**Step 2: 添加必要的导入**

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
```

**Step 3: 在CheckinService中添加listRecords方法**

```java
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
```

**Step 4: 扩展CheckinRecordRepository**

确保继承了 `BaseMapper<CheckinRecord>`，它已提供 `selectPage` 方法。

**Step 5: 验证**

```bash
curl "http://localhost:8080/api/admin/checkin/records?current=1&size=10" -H "Authorization: Bearer <token>"
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/admin/CheckinAdminController.java
git add backend/src/main/java/com/stadium/booking/service/CheckinService.java
git commit -m "feat: add checkin records list endpoint"
```

---

## Task 6: 修复前端API路径

**Files:**
- Modify: `admin-web/src/api/court.js`

**Step 1: 修改场地API路径**

将 `getCourtsByVenue` 的路径改为与后端一致：

```javascript
export const getCourtsByVenue = (venueId) => {
  return request({
    url: `/admin/courts/venue/${venueId}`
  })
}
```

**Step 2: Commit**

```bash
git add admin-web/src/api/court.js
git commit -m "fix: update court API path to match backend"
```

---

## Task 7: 集成测试

**Step 1: 启动后端服务**

```bash
cd backend
mvn spring-boot:run
```

**Step 2: 启动前端服务**

```bash
cd admin-web
npm run dev
```

**Step 3: 测试所有接口**

使用浏览器开发者工具或Postman测试以下场景：

1. 登录后获取用户信息
2. 查看场馆列表
3. 查看场地列表（分页）
4. 查看今日预约
5. 查看用户列表
6. 签到核销
7. 标记爽约

**Step 4: Commit**

```bash
git add -A
git commit -m "test: verify all API integrations"
```

---

## 验收标准

| 检查项 | 预期结果 |
|--------|----------|
| `POST /admin/auth/login` | 返回token和管理员信息 |
| `GET /admin/auth/profile` | 返回当前登录管理员信息 |
| `POST /admin/auth/logout` | 返回成功 |
| `GET /admin/bookings/today` | 返回今日预约列表 |
| `GET /admin/bookings/{bookingNo}` | 支持按预约号查询 |
| `GET /admin/courts` | 返回分页场地列表 |
| `GET /admin/courts/venue/{venueId}` | 返回场馆下的场地 |
| `GET /admin/users` | 返回分页用户列表 |
| `GET /admin/users/{id}` | 返回用户详情 |
| `GET /admin/users/{id}/bookings` | 返回用户预约记录 |
| `GET /admin/users/{id}/violations` | 返回用户违约记录 |
| `PATCH /admin/users/{id}/status` | 更新用户状态 |
| `GET /admin/checkin/records` | 返回签到记录列表 |

---

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 权限注解缺失 | 接口无权限控制 | 所有新接口都添加@RequirePermission |
| 数据库字段不存在 | 查询失败 | 检查User表结构，确保字段完整 |
| JWT Token过期 | 测试失败 | 使用有效token进行测试 |

---

## 完成标志

- [ ] 所有缺失接口已补充
- [ ] 所有路径不匹配问题已修复
- [ ] 后端编译无错误
- [ ] 所有接口返回正确响应
- [ ] 前端页面功能正常
