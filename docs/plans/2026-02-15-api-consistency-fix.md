# API 一致性问题修复实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修复后端服务与小程序端、管理端之间的接口一致性问题，确保数据结构、参数传递和类型定义完全同步。

**Architecture:** 采用分层修复策略，优先解决影响核心功能的严重问题（P0），然后处理中等问题（P1），最后优化轻微问题（P2）。修复顺序为：小程序端类型定义 → 管理端API调用 → 后端接口补充。

**Tech Stack:** 
- 后端: Java Spring Boot, MyBatis-Plus
- 小程序端: uni-app (Vue 3), TypeScript
- 管理端: Vue 3, Axios

---

## 问题优先级概览

| 优先级 | 问题编号 | 数量 | 预计工时 |
|-------|---------|------|---------|
| P0 (严重) | C-1, C-2, C-3 | 3 | 4h |
| P1 (中等) | M-1, M-2, M-3, M-4, M-5, M-6 | 6 | 3h |
| P2 (轻微) | m-3, m-4 | 2 | 1h |
| **总计** | - | **11** | **8h** |

---

## Phase 1: P0 严重问题修复 (预计 4 小时)

### Task 1: 修复登录响应结构不匹配 (C-2)

**优先级:** P0 - Critical  
**负责端:** 小程序端  
**预计时间:** 30 分钟

**Files:**
- Modify: `miniapp/src/types/user.ts`
- Modify: `miniapp/src/composables/useAuth.ts`

**Step 1: 更新 LoginResult 类型定义**

修改 `miniapp/src/types/user.ts`:

```typescript
export interface LoginResult {
  token: string
  refreshToken: string
  userId: number
  userType: string
  isNewUser: boolean
}
```

**Step 2: 更新 User 类型定义**

修改 `miniapp/src/types/user.ts`:

```typescript
export interface User {
  id: number
  name?: string
  phone?: string
  avatar?: string
  userType: number
  userTypeText?: string
  status?: number
  statusText?: string
  noShowCount?: number
  lastNoShowAt?: string
  bannedUntil?: string
  createdAt?: string
}
```

**Step 3: 更新 useAuth composable 登录逻辑**

修改 `miniapp/src/composables/useAuth.ts` 中的 login 函数:

```typescript
const login = async (): Promise<boolean> => {
  try {
    const loginRes = await new Promise<UniApp.LoginRes>((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: resolve,
        fail: reject
      })
    })

    const result: LoginResult = await wechatLogin(loginRes.code)
    
    token.value = result.token
    userInfo.value = {
      id: result.userId,
      userType: result.userType === 'STUDENT' ? 0 : result.userType === 'TEACHER' ? 1 : 2,
      userTypeText: result.userType
    }
    
    uni.setStorageSync('token', result.token)
    uni.setStorageSync('userInfo', userInfo.value)
    
    if (result.isNewUser) {
      uni.showToast({ title: '欢迎新用户', icon: 'success' })
    } else {
      uni.showToast({ title: '登录成功', icon: 'success' })
    }
    return true
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '登录失败', icon: 'none' })
    return false
  }
}
```

**Step 4: 验证修改**

运行小程序开发工具，测试微信登录流程，确认用户信息正确存储。

**Step 5: Commit**

```bash
git add miniapp/src/types/user.ts miniapp/src/composables/useAuth.ts
git commit -m "fix(miniapp): 修复登录响应结构不匹配问题 (C-2)"
```

---

### Task 2: 完善小程序端 Booking 类型定义 (C-3)

**优先级:** P0 - Critical  
**负责端:** 小程序端  
**预计时间:** 20 分钟

**Files:**
- Modify: `miniapp/src/types/booking.ts`

**Step 1: 更新 Booking 类型定义**

修改 `miniapp/src/types/booking.ts`:

```typescript
export interface Booking {
  id?: number
  bookingNo: string
  userId?: number
  userName?: string
  userPhone?: string
  venueId: number
  venueName: string
  courtId: number
  courtName: string
  bookingDate: string
  startTime: string
  endTime: string
  slotCount?: number
  bookingType?: number
  status: BookingStatus
  statusText?: string
  cancelReason?: string
  cancelledAt?: string
  checkedInAt?: string
  remark?: string
  createdAt: string
  qrToken?: string
}

export type BookingStatus = 1 | 2 | 3 | 4

export interface CreateBookingParams {
  venueId: number
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
  remark?: string
  bookingType?: number
}
```

**Step 2: 验证类型检查**

```bash
cd miniapp && npm run type-check
```

**Step 3: Commit**

```bash
git add miniapp/src/types/booking.ts
git commit -m "fix(miniapp): 完善 Booking 类型定义 (C-3)"
```

---

### Task 3: 添加后端用户资料接口 (C-1)

**优先级:** P0 - Critical  
**负责端:** 后端  
**预计时间:** 1.5 小时

**Files:**
- Create: `backend/src/main/java/com/stadium/booking/controller/api/UserController.java`
- Create: `backend/src/main/java/com/stadium/booking/dto/request/UserProfileUpdateRequest.java`

**Step 1: 创建 UserProfileUpdateRequest DTO**

创建 `backend/src/main/java/com/stadium/booking/dto/request/UserProfileUpdateRequest.java`:

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;
    
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
    
    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatar;
}
```

**Step 2: 创建 UserController**

创建 `backend/src/main/java/com/stadium/booking/controller/api/UserController.java`:

```java
package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.UserProfileUpdateRequest;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.security.UserContext;
import com.stadium.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户API", description = "小程序端用户接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(userService.getUserDetail(userId));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(userService.updateProfile(userId, request));
    }
}
```

**Step 3: 在 UserService 中添加 updateProfile 方法**

修改 `backend/src/main/java/com/stadium/booking/service/UserService.java`，添加:

```java
public UserResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    
    if (request.getName() != null) {
        user.setName(request.getName());
    }
    if (request.getPhone() != null) {
        user.setPhone(request.getPhone());
    }
    if (request.getAvatar() != null) {
        user.setAvatar(request.getAvatar());
    }
    
    userRepository.save(user);
    return getUserDetail(userId);
}
```

**Step 4: 更新 SecurityConfig 放行用户接口**

修改 `backend/src/main/java/com/stadium/booking/config/SecurityConfig.java`:

```java
.requestMatchers("/health", "/auth/**", "/admin/auth/**", "/user/**").permitAll()
```

**Step 5: 编译验证**

```bash
cd backend && mvn compile
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/api/UserController.java \
        backend/src/main/java/com/stadium/booking/dto/request/UserProfileUpdateRequest.java \
        backend/src/main/java/com/stadium/booking/service/UserService.java \
        backend/src/main/java/com/stadium/booking/config/SecurityConfig.java
git commit -m "feat(backend): 添加用户资料管理接口 (C-1)"
```

---

## Phase 2: P1 中等问题修复 (预计 3 小时)

### Task 4: 完善小程序端 Venue 类型定义 (M-3)

**优先级:** P1 - Medium  
**负责端:** 小程序端  
**预计时间:** 15 分钟

**Files:**
- Modify: `miniapp/src/types/venue.ts`

**Step 1: 更新 Venue 类型定义**

修改 `miniapp/src/types/venue.ts`:

```typescript
export interface Venue {
  id: number
  name: string
  code?: string
  sportType: string
  location: string
  description?: string
  imageUrl?: string
  openDays?: string
  openTime: string
  closeTime: string
  slotMinutes?: number
  bookAheadDays?: number
  cancelCutoffMinutes?: number
  dailySlotLimit?: number
  status: number
  courtCount?: number
  courts?: Court[]
}
```

**Step 2: Commit**

```bash
git add miniapp/src/types/venue.ts
git commit -m "fix(miniapp): 完善 Venue 类型定义 (M-3)"
```

---

### Task 5: 完善小程序端 Court 类型定义 (M-4)

**优先级:** P1 - Medium  
**负责端:** 小程序端  
**预计时间:** 10 分钟

**Files:**
- Modify: `miniapp/src/types/venue.ts`

**Step 1: 更新 Court 类型定义**

修改 `miniapp/src/types/venue.ts`:

```typescript
export interface Court {
  id: number
  venueId?: number
  venueName?: string
  name: string
  courtNo?: string
  sportType?: string
  floorType?: string
  features?: string
  status: number
  statusReason?: string
  statusUntil?: string
  sortOrder?: number
}
```

**Step 2: Commit**

```bash
git add miniapp/src/types/venue.ts
git commit -m "fix(miniapp): 完善 Court 类型定义 (M-4)"
```

---

### Task 6: 完善小程序端 TimeSlot 类型定义 (M-5)

**优先级:** P1 - Medium  
**负责端:** 小程序端  
**预计时间:** 10 分钟

**Files:**
- Modify: `miniapp/src/types/venue.ts`

**Step 1: 更新 TimeSlot 类型定义**

修改 `miniapp/src/types/venue.ts`:

```typescript
export interface TimeSlot {
  courtId?: number
  courtName?: string
  date?: string
  startTime: string
  endTime: string
  status: string
  booking?: BookingInfo
}

export interface BookingInfo {
  bookingNo: string
  userName: string
  userPhone: string
}
```

**Step 2: Commit**

```bash
git add miniapp/src/types/venue.ts
git commit -m "fix(miniapp): 完善 TimeSlot 类型定义 (M-5)"
```

---

### Task 7: 完善小程序端 User 类型定义 (M-6)

**优先级:** P1 - Medium  
**负责端:** 小程序端  
**预计时间:** 10 分钟

**Files:**
- Modify: `miniapp/src/types/user.ts`

**Step 1: 更新 User 类型定义**

已在 Task 1 中完成，确认包含所有字段。

**Step 2: 添加 userType 转换工具函数**

修改 `miniapp/src/composables/useAuth.ts`:

```typescript
export function useUserType() {
  const userTypeMap: Record<number, string> = {
    0: '学生',
    1: '教师',
    2: '教职工'
  }

  const userTypeCodeMap: Record<string, number> = {
    'STUDENT': 0,
    'TEACHER': 1,
    'STAFF': 2
  }

  const getUserTypeName = (type: number): string => {
    return userTypeMap[type] || '用户'
  }

  const getUserTypeCode = (type: string): number => {
    return userTypeCodeMap[type] ?? 0
  }

  return {
    getUserTypeName,
    getUserTypeCode
  }
}
```

**Step 3: Commit**

```bash
git add miniapp/src/composables/useAuth.ts
git commit -m "fix(miniapp): 完善 User 类型定义和转换函数 (M-6)"
```

---

### Task 8: 修复管理端取消预约接口参数 (M-1)

**优先级:** P1 - Medium  
**负责端:** 管理端  
**预计时间:** 10 分钟

**Files:**
- Modify: `admin-web/src/api/booking.js`

**Step 1: 更新 cancelBooking 函数**

修改 `admin-web/src/api/booking.js`:

```javascript
export const cancelBooking = (bookingNo, reason) => {
  return request({
    url: `/admin/bookings/${bookingNo}/cancel`,
    method: 'POST',
    params: { reason }
  })
}
```

**Step 2: Commit**

```bash
git add admin-web/src/api/booking.js
git commit -m "fix(admin): 取消预约接口添加 reason 参数支持 (M-1)"
```

---

### Task 9: 修复管理端场地状态更新接口参数 (M-2)

**优先级:** P1 - Medium  
**负责端:** 管理端  
**预计时间:** 10 分钟

**Files:**
- Modify: `admin-web/src/api/court.js`

**Step 1: 更新 updateCourtStatus 函数**

修改 `admin-web/src/api/court.js`:

```javascript
export const updateCourtStatus = (id, status, reason) => {
  return request({
    url: `/admin/courts/${id}/status`,
    method: 'PATCH',
    params: { status, reason }
  })
}
```

**Step 2: Commit**

```bash
git add admin-web/src/api/court.js
git commit -m "fix(admin): 场地状态更新接口添加 reason 参数支持 (M-2)"
```

---

## Phase 3: P2 轻微问题修复 (预计 1 小时)

### Task 10: 修复小程序端 GET 请求参数传递方式 (m-4)

**优先级:** P2 - Minor  
**负责端:** 小程序端  
**预计时间:** 15 分钟

**Files:**
- Modify: `miniapp/src/api/venue.js`
- Modify: `miniapp/src/api/booking.js`

**Step 1: 修改 getTimeSlots 函数**

修改 `miniapp/src/api/venue.js`:

```javascript
export const getTimeSlots = (venueId, courtId, date) => {
  return request({
    url: `/venues/${venueId}/courts/${courtId}/slots`,
    params: { date }
  })
}
```

**Step 2: 修改 getMyBookings 函数**

修改 `miniapp/src/api/booking.js`:

```javascript
export const getMyBookings = (status) => {
  return request({
    url: '/bookings/my',
    params: status ? { status } : {}
  })
}
```

**Step 3: 更新 request.js 支持 params**

修改 `miniapp/src/utils/request.js`:

```javascript
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    let url = BASE_URL + options.url
    let data = options.data
    
    if (options.params && Object.keys(options.params).length > 0) {
      const queryString = Object.entries(options.params)
        .filter(([_, value]) => value !== undefined && value !== null)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&')
      if (queryString) {
        url += `?${queryString}`
      }
    }
    
    uni.request({
      url,
      method: options.method || 'GET',
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({ url: '/pages/my/my' })
            reject(new Error('未登录'))
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(new Error(res.data.message))
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
```

**Step 4: Commit**

```bash
git add miniapp/src/api/venue.js miniapp/src/api/booking.js miniapp/src/utils/request.js
git commit -m "fix(miniapp): 修复 GET 请求参数传递方式 (m-4)"
```

---

### Task 11: 后端支持我的预约状态筛选 (m-3)

**优先级:** P2 - Minor  
**负责端:** 后端  
**预计时间:** 20 分钟

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/api/BookingApiController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/BookingService.java`

**Step 1: 修改 BookingApiController**

修改 `backend/src/main/java/com/stadium/booking/controller/api/BookingApiController.java`:

```java
@Operation(summary = "获取我的预约列表")
@GetMapping("/my")
public Result<List<BookingResponse>> getMyBookings(
        @RequestParam(required = false) Integer status) {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
        return Result.error(401, "请先登录");
    }
    return Result.success(bookingService.getUserBookings(userId, status));
}
```

**Step 2: 修改 BookingService**

修改 `backend/src/main/java/com/stadium/booking/service/BookingService.java`:

```java
public List<BookingResponse> getUserBookings(Long userId, Integer status) {
    LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Booking::getUserId, userId);
    
    if (status != null) {
        wrapper.eq(Booking::getStatus, status);
    }
    
    wrapper.orderByDesc(Booking::getCreatedAt);
    
    return bookingRepository.selectList(wrapper).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
}
```

**Step 3: 编译验证**

```bash
cd backend && mvn compile
```

**Step 4: Commit**

```bash
git add backend/src/main/java/com/stadium/booking/controller/api/BookingApiController.java \
        backend/src/main/java/com/stadium/booking/service/BookingService.java
git commit -m "feat(backend): 我的预约接口支持状态筛选 (m-3)"
```

---

## Phase 4: 集成测试与验证 (预计 1 小时)

### Task 12: 运行后端测试

**Step 1: 运行单元测试**

```bash
cd backend && mvn test
```

**Step 2: 启动后端服务**

```bash
cd backend && mvn spring-boot:run
```

**Step 3: 验证接口**

使用 curl 或 Postman 测试新增接口:

```bash
curl -X GET http://localhost:8080/api/user/profile -H "Authorization: Bearer <token>"
```

---

### Task 13: 运行前端类型检查

**Step 1: 小程序端类型检查**

```bash
cd miniapp && npm run type-check
```

**Step 2: 管理端构建测试**

```bash
cd admin-web && npm run build
```

---

### Task 14: 最终提交与文档更新

**Step 1: 更新 API 一致性报告**

标记所有问题为已修复。

**Step 2: 创建最终提交**

```bash
git add docs/api-consistency-report.md
git commit -m "docs: 更新 API 一致性报告，标记已修复问题"
```

---

## 成功标准

| 检查项 | 验证方法 | 预期结果 |
|-------|---------|---------|
| 登录功能 | 小程序端登录测试 | 用户信息正确存储 |
| 类型检查 | `npm run type-check` | 无类型错误 |
| 后端编译 | `mvn compile` | 编译成功 |
| 后端测试 | `mvn test` | 所有测试通过 |
| 接口调用 | Postman/curl 测试 | 所有接口正常响应 |

---

## 风险与缓解措施

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| 类型修改影响现有功能 | 中 | 逐步修改，保留可选字段 |
| 后端接口变更需要数据库迁移 | 低 | 仅添加接口，不修改表结构 |
| 小程序端缓存问题 | 低 | 清除缓存后重新测试 |

---

## 时间线

| 阶段 | 任务 | 预计时间 | 完成标志 |
|-----|------|---------|---------|
| Phase 1 | P0 严重问题 | 4h | 登录功能正常 |
| Phase 2 | P1 中等问题 | 3h | 类型检查通过 |
| Phase 3 | P2 轻微问题 | 1h | 所有接口正常 |
| Phase 4 | 集成测试 | 1h | 测试全部通过 |

**总计: 9 小时**

---

*计划创建完成*
