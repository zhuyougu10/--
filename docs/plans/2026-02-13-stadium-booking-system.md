# 校园多球馆智能预约系统 实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 构建一个校园多球馆、多场地的智能预约系统，包含微信小程序端、Web后台管理端和Spring Boot后端。

**Architecture:** 采用前后端分离架构，后端提供统一REST API，小程序端使用uni-app构建，后台管理端使用Vue 3。数据库采用MySQL，支持RBAC权限控制。

**Tech Stack:** Spring Boot 3, Vue 3, uni-app, MySQL, JWT, Redis

---

## 系统概述

### 三端架构
| 端 | 技术栈 | 用户角色 |
|---|--------|---------|
| 小程序端 | uni-app (微信小程序) | 学生/教师 |
| 后台管理端 | Vue 3 | 管理员、场馆员 |
| 后端服务 | Spring Boot 3 | 提供统一API |

### 核心功能模块
1. **用户认证与授权** - 微信登录、JWT、RBAC
2. **球馆/场地管理** - 资源CRUD、状态管理
3. **时段与可用性** - 时段生成、可用性查询
4. **预约管理** - 创建、取消、状态流转
5. **智能推荐** - 可预约方案推荐
6. **二维码核销** - 签到核销、幂等处理
7. **违约与限制** - 爽约记录、限制策略
8. **统计与审计** - 数据看板、操作日志

---

## 任务文件索引

| 任务文件 | 描述 | 依赖 |
|---------|------|------|
| [task-01-database-design.md](./task-01-database-design.md) | 数据库设计与初始化 | 无 |
| [task-02-backend-core.md](./task-02-backend-core.md) | 后端核心框架搭建 | task-01 |
| [task-03-auth-module.md](./task-03-auth-module.md) | 认证授权模块 | task-02 |
| [task-04-venue-management.md](./task-04-venue-management.md) | 球馆场地管理 | task-03 |
| [task-05-booking-core.md](./task-05-booking-core.md) | 预约核心功能 | task-04 |
| [task-06-smart-recommendation.md](./task-06-smart-recommendation.md) | 智能推荐系统 | task-05 |
| [task-07-qr-checkin.md](./task-07-qr-checkin.md) | 二维码核销 | task-05 |
| [task-08-violation-system.md](./task-08-violation-system.md) | 违约与限制系统 | task-05 |
| [task-09-miniapp.md](./task-09-miniapp.md) | 小程序端开发 | task-03 |
| [task-10-admin-web.md](./task-10-admin-web.md) | 后台管理端开发 | task-03 |

---

## 执行顺序

```
Phase 1: 基础设施
├── task-01-database-design.md
└── task-02-backend-core.md

Phase 2: 认证授权
└── task-03-auth-module.md

Phase 3: 核心业务 (可并行)
├── task-04-venue-management.md
└── (等待 task-03 完成)

Phase 4: 预约系统 (串行依赖)
├── task-05-booking-core.md
├── task-06-smart-recommendation.md
├── task-07-qr-checkin.md
└── task-08-violation-system.md

Phase 5: 前端开发 (可并行)
├── task-09-miniapp.md
└── task-10-admin-web.md

Phase 6: 集成测试
└── 端到端测试与验收
```

---

## 验收标准

### 功能验收
- [ ] 并发创建预约不出现双重占用
- [ ] 可用性页面对预约变更在 1 秒内可见
- [ ] 二维码核销幂等：重复扫码不会重复写入
- [ ] 取消后立即释放时段
- [ ] 后台/场馆员接口具备严格的角色权限控制

### 性能验收
- [ ] 可用性查询（单场地/单日）<= 300ms
- [ ] 智能推荐接口响应 <= 300ms
- [ ] 支持常规校园负载并发

### 安全验收
- [ ] 所有API需JWT认证
- [ ] RBAC权限控制完整
- [ ] 敏感操作有审计日志

---

## 配置参数默认值

| 参数 | 默认值 | 说明 |
|-----|-------|------|
| `slot_minutes` | 60 | 时段长度(分钟) |
| `book_ahead_days` | 7 | 可提前预约天数 |
| `cancel_cutoff_minutes` | 30 | 取消截止时间(开始前分钟) |
| `checkin_window_before` | 15 | 核销窗口开始(开始前分钟) |
| `no_show_grace_minutes` | 15 | 爽约宽限期(开始后分钟) |
| `daily_slot_limit` | 2 | 个人日限额(时段数) |
| `no_show_threshold` | 3 | 爽约阈值(30天内) |
| `ban_days` | 7 | 违约禁用天数 |

---

## 错误码规范

| 错误码 | HTTP状态 | 说明 |
|-------|---------|------|
| `SLOT_CONFLICT` | 409 | 时段冲突 |
| `OUT_OF_OPEN_HOURS` | 400 | 不在营业时间 |
| `OUT_OF_BOOKING_WINDOW` | 400 | 超出可预约窗口 |
| `LIMIT_EXCEEDED` | 403 | 超过个人限额 |
| `CANCEL_NOT_ALLOWED` | 403 | 已超过取消截止时间 |
| `CHECKIN_NOT_ALLOWED` | 403 | 不在核销窗口 |
| `INVALID_REQUEST` | 400 | 参数非法 |
| `UNAUTHORIZED` | 401 | 未登录 |
| `FORBIDDEN` | 403 | 无权限 |
| `INTERNAL_ERROR` | 500 | 服务内部错误 |
