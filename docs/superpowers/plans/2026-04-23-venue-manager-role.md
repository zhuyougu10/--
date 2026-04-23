# 场馆管理员角色实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 复用现有 `VENUE_STAFF` 角色，实现“场馆管理员”仅能管理已分配球馆的数据与操作能力，并同步完成后台接口与管理端入口收口。

**Architecture:** 后端按“角色签发、权限校验、数据范围收口”三层实现，前端只负责隐藏无权限入口与约束筛选范围。数据关系复用现有 `venue_staff(admin_user_id, venue_id)`，避免引入新的角色码和重复模型。

**Tech Stack:** Spring Boot、MyBatis-Plus、MySQL/Flyway、Vue 3、Pinia、Vue Router、Ant Design Vue

---

## 目标文件映射

- `backend/src/main/java/com/stadium/booking/service/AuthService.java`
  - 修正后台登录时的角色签发逻辑
- `backend/src/main/java/com/stadium/booking/controller/admin/AdminAuthController.java`
  - 确认后台 profile 接口返回前端所需角色信息
- `backend/src/main/java/com/stadium/booking/dto/response/LoginResponse.java`
  - 如现有 profile/login 返回模型复用该 DTO，则补充角色字段
- `backend/src/main/java/com/stadium/booking/security/PermissionAspect.java`
  - 仅允许 `ADMIN` 直通，其他后台角色走真实权限判断
- `backend/src/main/java/com/stadium/booking/repository/RolePermissionRepository.java`
  - 角色权限查询支持 `VENUE_STAFF` 生效
- 与 `venue_staff` 相关的 repository/mapper/service 文件
  - 提供“当前管理员可管理球馆集合”统一查询能力
- `backend/src/main/java/com/stadium/booking/service/VenueService.java`
  - 球馆列表、详情、编辑按授权球馆收口
- `backend/src/main/java/com/stadium/booking/service/CourtService.java`
  - 场地列表、创建、编辑、删除按授权球馆收口
- `backend/src/main/java/com/stadium/booking/service/BookingService.java`
  - 预约分页、今日排场、取消、违约标记等按授权球馆收口
- `backend/src/main/java/com/stadium/booking/service/CheckinService.java`
  - 核销记录、扫码核销、手动核销按授权球馆收口
- `backend/src/main/java/com/stadium/booking/controller/admin/ViolationAdminController.java`
- `backend/src/main/java/com/stadium/booking/service/ViolationService.java`
  - 违约标记按授权球馆收口
- `backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java`
  - 收紧为 `VENUE_STAFF` 不可访问
- `admin-web/src/store/modules/user.js`
  - 正确消费后端角色字段，映射 `VENUE_STAFF` 文案
- `admin-web/src/router/index.js`
  - 根据角色隐藏无权限菜单与路由入口
- `admin-web/src/components/layout/MainLayout.vue`
  - 同步收口左侧导航中“仪表盘”“用户管理”等菜单项
- 与后台账号管理相关的前端页面和后端接口
  - 为 `ADMIN` 提供场馆管理员与球馆的绑定维护入口

### Task 1: 建立场馆管理员回归测试基线

**Files:**
- Create: `backend/src/test/java/com/stadium/booking/auth/AdminAuthServiceTest.java` 或等价测试文件
- Create: `backend/src/test/java/com/stadium/booking/security/PermissionAspectTest.java` 或等价测试文件
- Create: `backend/src/test/java/com/stadium/booking/service/VenueScopeTest.java` 或等价测试文件
- Create: `backend/src/test/java/com/stadium/booking/service/BookingScopeTest.java` 或等价测试文件
- Create: `backend/src/test/java/com/stadium/booking/service/CheckinScopeTest.java` 或等价测试文件
- Modify: 现有测试基建文件（如存在统一测试基类）

- [ ] **Step 1: 为后台登录角色签发补一个失败测试**

编写测试覆盖：拥有 `VENUE_STAFF` 角色的后台账号登录后，token 中角色不是 `ADMIN`。

- [ ] **Step 2: 运行单测，确认它先失败**

Run: `mvn -q -Dtest=*Auth*Test test`
Expected: 失败，表现为角色仍被签发为 `ADMIN` 或当前尚无对应测试类/断言支持

- [ ] **Step 3: 为场馆范围收口补失败测试**

编写测试覆盖：`VENUE_STAFF` 查询球馆、预约、核销时只能拿到已授权球馆数据；访问未授权球馆时报权限错误；未分配球馆时列表返回空且定向操作失败。

- [ ] **Step 4: 运行对应测试，确认范围测试先失败**

Run: `mvn -q -Dtest=*Venue*Test,*Booking*Test,*Checkin*Test test`
Expected: 失败，表现为当前服务返回了越权数据、未做权限拦截，或当前尚无对应测试载体

- [ ] **Step 5: 提交当前测试设计到计划记录**

把新增测试目标同步到 `progress.md`，便于后续逐项核对。

### Task 2: 修正后台登录角色签发

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/service/AuthService.java`
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/AdminAuthController.java`
- Modify: `backend/src/main/java/com/stadium/booking/dto/response/LoginResponse.java`（如 profile/login 复用该 DTO）
- Modify: 与后台管理员角色查询相关的 repository/service 文件
- Test: `backend/src/test/java/com/stadium/booking/...Auth...`

- [ ] **Step 1: 写出最小失败断言**

覆盖以下行为：
- `ADMIN` 登录签发 `ADMIN`
- `VENUE_STAFF` 登录签发 `VENUE_STAFF`
- 无后台角色账号拒绝登录

- [ ] **Step 2: 跑测试确认失败**

Run: `mvn -q -Dtest=*Auth*Test test`
Expected: FAIL，原因是当前代码固定签发 `ADMIN`

- [ ] **Step 3: 最小实现角色判定逻辑**

在 `AuthService` 中查询管理员实际角色，按优先级 `ADMIN > VENUE_STAFF` 签发。

- [ ] **Step 4: 再跑测试确认通过**

Run: `mvn -q -Dtest=*Auth*Test test`
Expected: PASS

- [ ] **Step 5: 补齐 `/admin/auth/profile` 的角色返回**

前端当前依赖 profile 初始化 `userInfo.role`，因此必须明确让 profile 返回机器值角色码，例如 `ADMIN` 或 `VENUE_STAFF`，而不是把这一步设为可选。

### Task 3: 修正后台权限切面直通逻辑

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/security/PermissionAspect.java`
- Modify: `backend/src/main/java/com/stadium/booking/repository/RolePermissionRepository.java`
- Test: `backend/src/test/java/com/stadium/booking/...Permission...`

- [ ] **Step 1: 先写失败测试**

覆盖以下行为：
- `ADMIN` 可直接通过
- `VENUE_STAFF` 必须匹配 `role_permission`
- 无权限的 `VENUE_STAFF` 被拒绝

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=*Permission*Test test`
Expected: FAIL，原因是后台账号当前被统一放行

- [ ] **Step 3: 最小实现切面修复**

仅保留 `ADMIN` 直通；其余后台角色走真实权限校验。

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -q -Dtest=*Permission*Test test`
Expected: PASS

- [ ] **Step 5: 检查 `VENUE_STAFF` 权限配置是否足够**

若权限不足，记录缺口，留到迁移任务补齐。

### Task 4: 补齐场馆管理员权限数据与范围查询组件

**Files:**
- Create/Modify: 与 `venue_staff` 查询相关的 repository / mapper / service 文件
- Create/Modify: `backend/src/main/resources/db/migration/V*_*.sql`
- Test: 对应 repository/service 测试

- [ ] **Step 1: 先写失败测试**

覆盖以下行为：
- 可查询当前管理员绑定的球馆 ID 集合
- 未绑定球馆时返回空集
- 多对多关系可正确返回多个球馆
- 同一球馆绑定多个管理员时，多个管理员都能拿到该球馆授权

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=*VenueStaff*Test test`
Expected: FAIL，原因是当前无对应查询能力或实现不完整

- [ ] **Step 3: 最小实现范围查询组件**

提供统一方法：
- 获取当前管理员可管理球馆 ID 集合
- 判断目标球馆是否授权

- [ ] **Step 4: 如权限点不足，新增迁移补齐 `role_permission`**

只新增迁移，不改历史脚本。补到满足“球馆信息编辑、场地、预约、核销”边界为止。

- [ ] **Step 5: 再跑测试确认通过**

Run: `mvn -q -Dtest=*VenueStaff*Test test`
Expected: PASS

### Task 5: 收口球馆与场地服务

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/service/VenueService.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/CourtService.java`
- Modify: 相关 repository / mapper
- Test: `backend/src/test/java/com/stadium/booking/...Venue...`
- Test: `backend/src/test/java/com/stadium/booking/...Court...`

- [ ] **Step 1: 写失败测试**

覆盖以下行为：
- `VENUE_STAFF` 的球馆列表只返回授权球馆
- `VENUE_STAFF` 仅可编辑授权球馆
- `VENUE_STAFF` 不可新建/删除球馆
- `VENUE_STAFF` 仅可管理授权球馆下的场地

- [ ] **Step 2: 跑测试确认失败**

Run: `mvn -q -Dtest=*Venue*Test,*Court*Test test`
Expected: FAIL

- [ ] **Step 3: 最小实现球馆服务收口**

按当前登录角色区分：
- `ADMIN` 全量
- `VENUE_STAFF` 限制为授权球馆

- [ ] **Step 4: 最小实现场地服务收口**

对列表、创建、更新、删除都增加授权校验。

- [ ] **Step 5: 再跑测试确认通过**

Run: `mvn -q -Dtest=*Venue*Test,*Court*Test test`
Expected: PASS

### Task 6: 收口预约与核销服务

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/service/BookingService.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/CheckinService.java`
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/ViolationAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/ViolationService.java`
- Modify: 相关 repository / mapper
- Test: `backend/src/test/java/com/stadium/booking/...Booking...`
- Test: `backend/src/test/java/com/stadium/booking/...Checkin...`

- [ ] **Step 1: 写失败测试**

覆盖以下行为：
- 预约分页仅返回授权球馆
- 今日排场仅返回授权球馆
- 非授权球馆预约不可取消/不可标记违约
- 非授权球馆预约不可核销

- [ ] **Step 2: 跑测试确认失败**

Run: `mvn -q -Dtest=*Booking*Test,*Checkin*Test test`
Expected: FAIL

- [ ] **Step 3: 最小实现预约服务收口**

对列表与操作接口统一加球馆范围校验。

- [ ] **Step 4: 最小实现核销服务收口**

对扫码核销、手动核销、核销记录增加授权校验。

- [ ] **Step 5: 最小实现违约服务收口**

对 `markNoShow` 等违约管理入口增加授权校验，确保通过 `bookingId` 间接关联到球馆后再判断范围。

- [ ] **Step 6: 再跑测试确认通过**

Run: `mvn -q -Dtest=*Booking*Test,*Checkin*Test test`
Expected: PASS

### Task 7: 收紧后台账号与统计访问

**Files:**
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java`
- Modify: 如存在统计 controller/service，则同步收紧；如不存在，则只覆盖当前真实存在的用户管理入口
- Test: 对应接口测试

- [ ] **Step 1: 写失败测试**

覆盖以下行为：
- `VENUE_STAFF` 无法访问用户管理接口
- `VENUE_STAFF` 无法访问后台账号管理或统计接口（若当前模块存在）

- [ ] **Step 2: 跑测试确认失败**

Run: `mvn -q -Dtest=*User*Test,*Stats*Test test`
Expected: FAIL

- [ ] **Step 3: 最小实现权限收紧**

通过权限点或控制器限制，只允许 `ADMIN` 访问用户管理，以及项目中真实存在的后台账号管理/统计入口。

- [ ] **Step 4: 再跑测试确认通过**

Run: `mvn -q -Dtest=*User*Test,*Stats*Test test`
Expected: PASS

- [ ] **Step 5: 复核不会影响 `ADMIN` 现有能力**

补一个正向测试，确保超级管理员访问仍正常。

### Task 8: 管理端菜单与筛选范围收口

**Files:**
- Modify: `admin-web/src/store/modules/user.js`
- Modify: `admin-web/src/router/index.js`
- Modify: 菜单配置、布局文件、球馆/场地/预约/核销相关页面
- Test: 若项目有前端单测则补；若没有，记录手工验证步骤

- [ ] **Step 1: 先补一个最小前端角色映射测试或验证点**

覆盖 `VENUE_STAFF` 显示为“场馆管理员”，且不展示仪表盘、用户管理、后台账号管理、统计菜单。

- [ ] **Step 2: 实现最小前端收口**

根据 profile 返回的机器值角色码隐藏无权限菜单，并限制球馆筛选项只显示授权球馆；同时隐藏球馆管理中的“新建球馆”“删除球馆”等 `ADMIN` 专属操作。

- [ ] **Step 3: 运行管理端构建验证**

Run: `npm run build`
Workdir: `admin-web`
Expected: build 成功

- [ ] **Step 4: 做手工回归检查**

检查：
- `ADMIN` 菜单不受影响
- `VENUE_STAFF` 看不到仪表盘、用户管理、后台账号和统计入口
- 授权球馆筛选正确

- [ ] **Step 5: 把前端验证结果写入 `progress.md`**

记录实际页面与角色行为验证结果。

### Task 9: 管理员与球馆绑定维护入口

**Files:**
- Modify: 后台账号管理相关后端接口与 DTO
- Modify: 后台账号管理相关前端页面
- Test: 对应后端测试与前端构建验证

- [ ] **Step 1: 先写失败测试或接口验证点**

覆盖以下行为：
- `ADMIN` 可以查看某后台账号当前绑定的球馆
- `ADMIN` 可以更新后台账号与球馆的多对多绑定
- `VENUE_STAFF` 无法执行上述管理动作

- [ ] **Step 2: 运行测试或最小接口验证，确认现状不支持**

Run: `mvn -q -Dtest=*User*Test test`
Expected: FAIL 或当前无对应接口

- [ ] **Step 3: 最小实现后端绑定维护能力**

为 `ADMIN` 提供查询和保存 `venue_staff` 绑定关系的接口，不做超出当前需求的账号体系重构。

- [ ] **Step 4: 最小实现前端维护入口**

在后台账号管理页中增加“管理球馆”能力，支持为场馆管理员分配多个球馆。

- [ ] **Step 5: 验证绑定变更能即时影响数据范围**

更新绑定后重新获取 profile 和业务数据，确认授权范围立即生效。

- [ ] **Step 6: 验证新增绑定、移除绑定与重复绑定处理**

覆盖以下行为：
- 新增绑定后可访问对应球馆
- 移除绑定后不可再访问对应球馆
- 重复保存同一 `(admin_user_id, venue_id)` 关系不会产生重复数据或重复展示

### Task 10: 总体验证与收尾

**Files:**
- Modify: `task_plan.md`
- Modify: `findings.md`
- Modify: `progress.md`

- [ ] **Step 1: 运行后端关键测试与编译**

Run: `mvn -q test`
Expected: 全部相关测试通过

- [ ] **Step 2: 运行后端编译兜底验证**

Run: `mvn -q -DskipTests compile`
Expected: compile 成功

- [ ] **Step 3: 运行管理端构建**

Run: `npm run build`
Workdir: `admin-web`
Expected: build 成功

- [ ] **Step 4: 更新规划文件**

在 `task_plan.md` 标记阶段完成，在 `findings.md` 记录最终设计落地结果，在 `progress.md` 记录测试与验证输出。

- [ ] **Step 5: 输出验收清单**

整理一份面向验收的检查项：
- 角色显示正确
- 绑定球馆范围正确
- 新增/移除球馆绑定后重新获取 profile 可见范围正确
- 越权访问被拦截
- 超级管理员能力未回归
