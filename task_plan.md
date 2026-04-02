# Task Plan: 校园多球馆智能预约系统实施计划

## Goal
根据需求文档 `docs/requirements.md` 制定详细的实施计划，将系统分解为多个独立任务文件，每个任务文件包含完整的实现步骤和代码示例。

## Current Phase
Phase 5 - 完成

## Phases

### Phase 1: 基础设施
- [x] 创建 docs/plans 目录结构
- [x] 创建主实施计划文件 (2026-02-13-stadium-booking-system.md)
- **Status:** complete

### Phase 2: 后端核心任务
- [x] 创建数据库设计任务文件 (task-01-database-design.md)
- [x] 创建后端核心框架任务文件 (task-02-backend-core.md)
- [x] 创建认证授权模块任务文件 (task-03-auth-module.md)
- [x] 创建球馆场地管理任务文件 (task-04-venue-management.md)
- **Status:** complete

### Phase 3: 预约系统任务
- [x] 创建预约核心功能任务文件 (task-05-booking-core.md)
- [x] 创建智能推荐任务文件 (task-06-smart-recommendation.md)
- [x] 创建二维码核销任务文件 (task-07-qr-checkin.md)
- [x] 创建违约限制系统任务文件 (task-08-violation-system.md)
- **Status:** complete

### Phase 4: 前端开发任务
- [x] 创建小程序端任务文件 (task-09-miniapp.md)
- [x] 创建后台管理端任务文件 (task-10-admin-web.md)
- **Status:** complete

### Phase 5: 文档更新
- [x] 更新 task_plan.md
- [x] 更新 progress.md
- **Status:** complete

## Key Questions
1. ✅ 如何组织任务文件结构？ → 按功能模块划分，每个任务独立
2. ✅ 每个任务文件需要包含什么？ → 目标、步骤、代码、验证、提交
3. ✅ 任务之间的依赖关系如何？ → 通过依赖声明明确先后顺序

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 使用独立任务文件 | 便于并行开发、独立审查、增量实施 |
| 每个任务包含完整代码 | 工程师可直接复制使用，减少理解成本 |
| 明确依赖关系 | 避免实施顺序错误，保证系统可构建 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| 无 | - | - |

## Notes
- 所有任务文件已创建完成
- 任务文件位于 `docs/plans/` 目录
- 主计划文件为 `2026-02-13-stadium-booking-system.md`
- 共创建 10 个独立任务文件

---

## Task Plan: 管理端预约记录与今日排场 SQL 报错修复（2026-04-02）

## Goal
修复管理端接口 `/api/admin/bookings` 与 `/api/admin/bookings/today` 的数据库报错：`Unknown column 'deleted_at' in 'field list'`，确保分页查询与今日排场查询恢复可用。

## Current Phase
Phase 3 - 完成

## Phases

### Phase 1: 根因定位
- [x] 复现与分析异常日志
- [x] 对照 `BookingRepository` 与 `booking` 表结构
- [x] 确认根因：`booking` 表缺少 `deleted_at` 字段
- **Status:** complete

### Phase 2: 修复实施
- [x] 设计 Flyway 前向迁移方案（不改历史迁移版本）
- [x] 新增迁移脚本为 `booking` 表补齐 `deleted_at`
- [x] 本地验证迁移与编译
- **Status:** complete

### Phase 3: 收尾与验证建议
- [x] 输出验证步骤（接口与数据库）
- [x] 更新 `progress.md` 与 `findings.md`
- **Status:** complete

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 使用新增 Flyway 迁移脚本修复 | 避免修改已存在迁移文件带来的 checksum 风险 |
| 保留 `BookingRepository` 中 `deleted_at` 过滤 | 与现有软删除模型和 `BaseEntity` 逻辑保持一致 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| `Unknown column 'deleted_at'` | 1 | 通过 schema 对照确认 `booking` 表缺列，采用新增迁移补齐 |
| `Migration checksum mismatch for version 8` | 1 | 数据库已存在历史 V8 记录，迁移改为 V9 |
| `OutOfMemoryError: Java heap space`（时段接口） | 1 | 修复时段生成循环在午夜回绕时无法终止的问题 |

---

## Task Plan: 球馆图片上传与小程序展示（2026-04-02）

## Goal
实现管理端球馆图片上传能力，并在小程序端球馆列表/详情页展示真实球馆图片。

## Current Phase
Phase 3 - 完成

## Phases

### Phase 1: 现状梳理
- [x] 确认现有球馆实体与 DTO 已包含 `imageUrl`
- [x] 确认管理端表单无上传能力
- [x] 确认小程序端使用占位图兜底
- **Status:** complete

### Phase 2: 后端与管理端实现
- [x] 新增后台图片上传接口 `/admin/files/upload`
- [x] 新增上传返回 DTO
- [x] 新增静态资源映射 `/uploads/**` 与 `/api/uploads/**`
- [x] 管理端球馆表单接入上传组件与预览
- **Status:** complete

### Phase 3: 小程序展示与验证
- [x] 新增图片 URL 解析工具（相对路径转绝对地址）
- [x] 更新小程序球馆相关页面图片展示逻辑
- [x] 完成后端编译与管理端构建验证
- **Status:** complete

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 使用本地磁盘存储上传图片 | 实现成本低、可快速打通管理端到小程序端链路 |
| 上传接口限制格式与大小 | 降低非法文件与超大文件风险 |
| 小程序端统一做相对 URL 解析 | 兼容后端返回相对路径与绝对路径 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| miniapp 无 `npm run build` 脚本 | 1 | 使用后端编译 + 管理端构建验证，miniapp 通过代码静态改造完成 |

---

## Task Plan: 管理端扫码核销与登录移动端适配（2026-04-02）

## Goal
实现管理端扫码核销（扫码优先、手动降级），并将登录页改为响应式布局；手机端登录后自动进入扫码核销页面。

## Current Phase
Phase 3 - 完成

## Phases

### Phase 1: 现状与能力确认
- [x] 确认后端已存在扫码核销接口 `/admin/checkin/scan`
- [x] 确认管理端扫码页当前仅占位提示，未接入相机扫码
- [x] 确认登录页与路由跳转当前未区分移动端
- **Status:** complete

### Phase 2: 功能实现
- [x] 新增设备识别工具（移动端检测）
- [x] 登录成功后按端类型跳转（移动端到 `/checkin`）
- [x] 路由守卫补充已登录访问 `/login` 的移动端分流
- [x] 管理端扫码页接入相机扫码与 `BarcodeDetector`
- [x] 扫码失败/不支持时自动降级手动输入
- **Status:** complete

### Phase 3: UI 适配与验证
- [x] 登录页响应式布局优化
- [x] 扫码页移动端布局优化
- [x] 执行管理端构建验证
- **Status:** complete

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 扫码优先，手动输入降级 | 满足业务目标并保证兼容性 |
| 使用浏览器原生相机 + BarcodeDetector | 无额外依赖，落地快 |
| 登录后移动端自动跳 `/checkin` | 缩短移动端核销操作路径 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| user store 补丁首次匹配失败 | 1 | 重新读取文件后按真实上下文打补丁 |

---

## Task Plan: 小程序二维码可扫性与状态联动（2026-04-03）

## Goal
确保小程序预约详情页二维码为标准可扫码，并与管理端扫码核销流程稳定闭环；同时完善过期/完成状态与“我的”页红点逻辑。

## Current Phase
Phase 3 - 进行中

## Phases

### Phase 1: 状态逻辑改造
- [x] 预约详情进入自动生成二维码
- [x] 过期显示“已过期”并隐藏二维码
- [x] 核销后自动刷新为“已完成”
- [x] 我的页面红点排除过期记录
- **Status:** complete

### Phase 2: 二维码生成链路替换
- [x] 移除外链二维码依赖
- [x] 接入本地 Canvas 生成二维码
- [x] 处理 `qrcode` 库在 uni-app 的兼容问题
- **Status:** complete

### Phase 3: 可扫性优化与联调
- [ ] 调整二维码标准化参数（纠错级别、静区、像素整数化）
- [ ] 真机与管理端扫码页联调验证稳定可扫
- [ ] 输出最终验收结果与回归检查清单
- **Status:** in_progress

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 小程序二维码改为本地 Canvas 渲染 | 避免外链服务在小程序环境不可达 |
| 先完成状态逻辑，再集中攻克可扫性 | 降低问题耦合，便于定位扫码失败根因 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| 外链二维码请求失败 `ERR_CONNECTION_CLOSED` | 1 | 改为本地生成 |
| `qrcode` 库报 `You need to specify a canvas element` | 1 | 替换为 `qrcode-generator + uni.createCanvasContext` |
| 用户反馈二维码仍不可扫 | 1 | 继续优化渲染参数并准备真机联调 |
