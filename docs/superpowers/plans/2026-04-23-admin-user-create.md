# 管理端新增后台账号与预置用户 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为管理端补齐“新增场馆管理员”和“新增预置用户”能力，并修正普通用户页的现有接口契约问题。

**Architecture:** 复用现有两个列表页，通过弹窗表单触发新增请求。后端分别在后台账号管理和用户管理控制器中补充最小创建接口，服务层负责校验、落库与返回最新响应对象，并保持现有 `ADMIN` 权限边界不变。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3, Ant Design Vue

---

### Task 1: 后台账号创建后端测试与实现

**Files:**
- Create: `backend/src/main/java/com/stadium/booking/dto/request/AdminUserCreateRequest.java`
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/AdminUserAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/AdminUserManagementService.java`
- Modify: `backend/src/main/java/com/stadium/booking/repository/AdminUserRepository.java`
- Test: `backend/src/test/java/com/stadium/booking/service/AdminUserManagementServiceTest.java`

- [ ] Step 1: 写后台账号创建失败测试
- [ ] Step 2: 运行测试确认失败
- [ ] Step 3: 实现创建场馆管理员最小逻辑，并覆盖用户名唯一、密码必填、球馆必填、球馆 ID 有效、仅创建 `VENUE_STAFF`
- [ ] Step 4: 运行测试确认通过

### Task 2: 预置用户创建后端测试与实现

**Files:**
- Create: `backend/src/main/java/com/stadium/booking/dto/request/UserPresetCreateRequest.java`
- Modify: `backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java`
- Modify: `backend/src/main/java/com/stadium/booking/service/UserService.java`
- Modify: `backend/src/main/java/com/stadium/booking/repository/UserRepository.java`
- Test: `backend/src/test/java/com/stadium/booking/service/UserServiceTest.java`

- [ ] Step 1: 写预置用户创建失败测试
- [ ] Step 2: 运行测试确认失败
- [ ] Step 3: 实现预置待绑定用户最小逻辑，并覆盖工号/学号唯一、手机号唯一、用户类型必填、`openid/unionId` 为空、`isBound=0`
- [ ] Step 4: 运行测试确认通过

### Task 3: 权限边界与空身份字段验证

**Files:**
- Modify: `backend/src/test/java/com/stadium/booking/service/AdminUserManagementServiceTest.java`
- Modify: `backend/src/test/java/com/stadium/booking/service/UserServiceTest.java`

- [ ] Step 1: 增加 `ADMIN` 角色限制相关测试，确保新增能力仍只由管理端控制器对 `ADMIN` 开放
- [ ] Step 2: 增加待绑定用户空 `openid/unionId` 状态可被正常查询与转换的测试
- [ ] Step 3: 运行相关测试确认通过

### Task 4: 前端后台账号管理页新增入口

**Files:**
- Modify: `admin-web/src/views/admin-user/list.vue`
- Modify: `admin-web/src/api/admin-user.js`

- [ ] Step 1: 增加新增场馆管理员弹窗与表单
- [ ] Step 2: 接入创建 API 并刷新列表
- [ ] Step 3: 保持原有球馆分配能力不回归

### Task 5: 前端用户管理页新增入口与兼容修正

**Files:**
- Modify: `admin-web/src/views/user/list.vue`
- Modify: `admin-web/src/api/user.js`

- [ ] Step 1: 增加新增预置用户弹窗与表单
- [ ] Step 2: 接入创建 API 并刷新列表
- [ ] Step 3: 修正搜索参数与子表数据读取方式

### Task 6: 验证

**Files:**
- Modify: `task_plan.md`
- Modify: `findings.md`
- Modify: `progress.md`

- [ ] Step 1: 运行后端相关测试
- [ ] Step 2: 验证新增后台账号可出现在列表并保持“场馆管理员”角色展示
- [ ] Step 3: 验证预置用户可出现在列表并显示“未绑定”状态
- [ ] Step 4: 验证普通用户页搜索、预约记录、违约记录读取正常
- [ ] Step 5: 运行前端构建验证
- [ ] Step 6: 回写计划、发现与进度文件
