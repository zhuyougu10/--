# Findings & Decisions
<!-- 
  WHAT: Your knowledge base for the task. Stores everything you discover and decide.
  WHY: Context windows are limited. This file is your "external memory" - persistent and unlimited.
  WHEN: Update after ANY discovery, especially after 2 view/browser/search operations (2-Action Rule).
-->

## Requirements
<!-- 
  WHAT: What the user asked for, broken down into specific requirements.
  WHY: Keeps requirements visible so you don't forget what you're building.
  WHEN: Fill this in during Phase 1 (Requirements & Discovery).
  EXAMPLE:
    - Command-line interface
    - Add tasks
    - List all tasks
    - Delete tasks
    - Python implementation
-->
<!-- Captured from user request -->
- 修复管理端预约记录接口 `/api/admin/bookings` 报错
- 修复管理端今日排场接口 `/api/admin/bookings/today` 报错
- 错误为 MySQL `Unknown column 'deleted_at' in 'field list'`
- 管理端支持球馆图片上传
- 小程序端展示球馆真实图片（不再仅占位图）
- 管理端实现扫码核销能力
- 管理端登录页改为响应式布局，适配手机端
- 手机端登录后自动跳转扫码核销页面
- 小程序预约详情页进入时自动生成二维码
- 预约过期后显示红色“已过期”并隐藏二维码
- 核销成功后预约详情自动刷新为“已完成”
- 我的页面“待使用”红点需排除已过期记录，不新增“已过期”菜单
- 小程序二维码必须为标准可扫码，需与管理端扫码核销稳定联调

## Research Findings
<!-- 
  WHAT: Key discoveries from web searches, documentation reading, or exploration.
  WHY: Multimodal content (images, browser results) doesn't persist. Write it down immediately.
  WHEN: After EVERY 2 view/browser/search operations, update this section (2-Action Rule).
  EXAMPLE:
    - Python's argparse module supports subcommands for clean CLI design
    - JSON module handles file persistence easily
    - Standard pattern: python script.py <command> [args]
-->
<!-- Key discoveries during exploration -->
- `BookingRepository` 多个 SQL 显式使用 `deleted_at IS NULL` 作为软删除过滤条件
- `BookingService#listPage` 与 `BookingService#getTodayBookings` 通过 `BaseMapper` 查询，受 `BaseEntity.deletedAt` 逻辑删除字段映射影响
- `V1__init_schema.sql` 中 `booking` 表未定义 `deleted_at` 字段，但 `BaseEntity` 定义了 `deletedAt`，导致查询字段中包含 `deleted_at`
- 当前迁移版本已到 `V7`，适合新增更高版本迁移修复，不应回改历史 `V1`
- `TimeSlotService#generateTimeSlots` 使用 `LocalTime.plusMinutes` 在接近午夜时会回绕（如 `23:00 + 60min = 00:00`），若 `close_time=23:59` 会导致循环回到 `00:00` 并继续，形成近似无限循环，最终触发 OOM
- 后端 `Venue`、`VenueCreateRequest`、`VenueResponse` 已具备 `imageUrl` 字段，仅缺少文件上传链路
- 管理端 `venue/form.vue` 当前只提交文本字段，没有图片上传控件
- 小程序多个页面直接使用 `venue.imageUrl || '/static/default-venue.svg'`，当后端返回相对路径时无法直接展示
- 后端已具备扫码核销能力：`/admin/checkin/scan`，管理端未接入浏览器扫码
- 管理端 `checkin/index.vue` 当前“开始扫码”仅提示文案，不执行扫码
- 登录页 `login/index.vue` 与路由守卫当前固定跳 `/dashboard`，未区分移动端

## Technical Decisions
<!-- 
  WHAT: Architecture and implementation choices you've made, with reasoning.
  WHY: You'll forget why you chose a technology or approach. This table preserves that knowledge.
  WHEN: Update whenever you make a significant technical choice.
  EXAMPLE:
    | Use JSON for storage | Simple, human-readable, built-in Python support |
    | argparse with subcommands | Clean CLI: python todo.py add "task" |
-->
<!-- Decisions made with rationale -->
| Decision | Rationale |
|----------|-----------|
| 新增 Flyway 迁移为 `booking` 表补齐 `deleted_at` | 与实体/查询软删除约定一致，改动面最小 |
| 不移除仓储层 `deleted_at` 条件 | 统一软删除语义，避免数据泄漏与行为不一致 |
| 不修改历史迁移文件 | 避免 Flyway checksum 冲突与发布风险 |
| 管理端新增本地上传接口 `/admin/files/upload` | 在不引入外部存储依赖下最快落地 |
| 小程序新增统一 URL 解析工具 | 兼容相对路径与绝对路径，减少页面重复逻辑 |
| 管理端扫码采用 BarcodeDetector + getUserMedia | 无需引入额外依赖，移动端可快速落地 |
| 扫码不支持时自动降级手动输入 | 保障低版本设备可用性 |
| 登录/路由按移动端跳转核销页 | 满足手机端核销高频场景 |

## Issues Encountered
<!-- 
  WHAT: Problems you ran into and how you solved them.
  WHY: Similar to errors in task_plan.md, but focused on broader issues (not just code errors).
  WHEN: Document when you encounter blockers or unexpected challenges.
  EXAMPLE:
    | Empty file causes JSONDecodeError | Added explicit empty file check before json.load() |
-->
<!-- Errors and how they were resolved -->
| Issue | Resolution |
|-------|------------|
| 本地报错日志指向 `BookingRepository` SQL 字段不存在 | 对照实体与建表语句，确认 schema 漏字段并通过新迁移补齐 |
| 启动时报 Flyway V8 checksum mismatch | 数据库已存在 `V8__add_booking_rules.sql` 记录，避免复用版本号，改为新增 `V9` 迁移 |
| 启动时报 `Detected applied migration not resolved locally: 8` | 本地已无 `V8__add_booking_rules.sql` 文件，按 repair 语义将历史 8 标记为 `DELETE` 后恢复 |
| 时段接口切换日期后出现 OOM | 修复时段生成循环的午夜回绕问题，增加 `endTime` 前进性保护 |
| miniapp 缺少 build 脚本 | 使用现有工程方式进行运行态验证，不阻断功能实现 |
| BarcodeDetector 在部分浏览器不可用 | 前端自动提示并降级为手动输入核销 |
| 小程序外链二维码服务不可用（`ERR_CONNECTION_CLOSED`） | 改为本地 Canvas 生成二维码，避免依赖外部服务 |
| 小程序使用 `qrcode` 库报 `You need to specify a canvas element` | 改为 `qrcode-generator + uni.createCanvasContext` 本地绘制方案 |
| 用户反馈本地二维码仍“扫不了” | 继续提升二维码渲染标准化参数（纠错级别/静区/像素整数化）并与管理端真机联调 |

## Resources
<!-- 
  WHAT: URLs, file paths, API references, documentation links you've found useful.
  WHY: Easy reference for later. Don't lose important links in context.
  WHEN: Add as you discover useful resources.
  EXAMPLE:
    - Python argparse docs: https://docs.python.org/3/library/argparse.html
    - Project structure: src/main.py, src/utils.py
-->
<!-- URLs, file paths, API references -->
- `backend/src/main/java/com/stadium/booking/repository/BookingRepository.java`
- `backend/src/main/java/com/stadium/booking/entity/BaseEntity.java`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `backend/src/main/java/com/stadium/booking/controller/admin/FileAdminController.java`
- `admin-web/src/views/venue/form.vue`
- `miniapp/src/utils/asset.ts`
- `admin-web/src/views/checkin/index.vue`
- `admin-web/src/views/login/index.vue`
- `admin-web/src/router/index.js`

## Visual/Browser Findings
<!-- 
  WHAT: Information you learned from viewing images, PDFs, or browser results.
  WHY: CRITICAL - Visual/multimodal content doesn't persist in context. Must be captured as text.
  WHEN: IMMEDIATELY after viewing images or browser results. Don't wait!
  EXAMPLE:
    - Screenshot shows login form has email and password fields
    - Browser shows API returns JSON with "status" and "data" keys
-->
<!-- CRITICAL: Update after every 2 view/browser operations -->
<!-- Multimodal content must be captured as text immediately -->
- 用户提供接口请求与后端错误日志，两个接口均触发同一 SQL 错误：查询 `booking` 表时选择了 `deleted_at` 字段，但数据库不存在该列

---
<!-- 
  REMINDER: The 2-Action Rule
  After every 2 view/browser/search operations, you MUST update this file.
  This prevents visual information from being lost when context resets.
-->
*Update this file after every 2 view/browser/search operations*
*This prevents visual information from being lost*
