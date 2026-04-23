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
- 数据库初始化脚本已存在后台角色 `VENUE_STAFF`，并已定义管理员-球馆多对多关系表 `venue_staff(admin_user_id, venue_id)`
- 后端登录 `AuthService#adminLogin` 当前固定签发 `ADMIN` 角色，导致已有 `VENUE_STAFF` 设计没有真正进入运行态
- `PermissionAspect` 对 `principal.getIsAdmin()` 直接放行，当前后台 token 又固定 `isAdmin=true`，使后台细粒度权限校验基本被绕过
- `VenueService`、`CourtService`、`BookingService`、`CheckinService` 当前都没有基于“当前管理员可管理球馆”做范围收口
- 用户已确认场馆管理员能力边界：可管理自己球馆的场地、时段、预约、核销；不可管理后台账号和全局统计
- 用户已确认复用现有角色码 `VENUE_STAFF`，中文展示为“场馆管理员”
- 已输出正式规格文档 `docs/superpowers/specs/2026-04-23-venue-manager-role-design.md`
- 已输出正式实施计划 `docs/superpowers/plans/2026-04-23-venue-manager-role.md`
- 绑定关系落地不能只依赖数据库表，必须提供 `ADMIN` 可用的球馆分配入口
- `venue_staff` 需要保证 `(admin_user_id, venue_id)` 唯一，避免重复绑定造成查询与展示重复
- 绑定变更后的“即时生效”应以服务端每次请求实时查库为准，前端菜单通过重新获取 profile 或重新登录刷新
- 后端已新增 `AdminRoleRepository`、`VenueStaffRepository`、`AdminVenueAccessService`，把角色签发、权限切面、球馆范围判断三段串起来
- 后端已新增 `/admin/admin-users` 管理接口，用于 `ADMIN` 维护后台账号与球馆绑定关系
- 保存球馆绑定时会自动补齐 `VENUE_STAFF` 角色；清空绑定时会撤销该角色，避免账号继续登录后台
- `ADMIN` 账号禁止配置受限球馆，避免因登录优先级导致“看似受限、实际全局”的误配置
- `VENUE_STAFF` 登录时若未分配任何球馆，将被拒绝进入后台
- 已新增 `V10__update_venue_staff_permissions.sql`，移除 `VENUE_STAFF` 的 `stats:read`，补齐 `venue:update`、`court:create/update/delete`、`booking:cancel`
- 前端已新增“后台账号管理”页面，并按角色隐藏仪表盘、用户管理、后台账号等入口
- 前端移动端行为已调整为“登录后默认进入核销页，但不再持续锁死在核销页”，场馆管理员可继续访问其它管理页面

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
| 代码库虽已有 `VENUE_STAFF` 与 `venue_staff` 表，但后台仍相当于全局管理员模式 | 需要把登录签发、权限切面、业务范围校验三段链路同时补齐，否则新增角色只有名义没有约束 |
| 文档首轮审阅发现角色展示值、后台直通条件、零授权场景、用户管理入口、违约入口等口径不完整 | 已修正规格与实施计划，使其与当前仓库入口保持一致 |
| “给管理员账号分配球馆”会因为登录优先级造成假象权限收口 | 实现层改为禁止 `ADMIN` 账号配置受限球馆，只允许非管理员账号成为场馆管理员 |
| 移动端原有全局跳核销逻辑会把场馆管理员锁死在 `/checkin` | 调整为仅登录后默认进入核销页，后续路由不再强制覆盖 |

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
- `backend/src/main/java/com/stadium/booking/service/AuthService.java`
- `backend/src/main/java/com/stadium/booking/security/PermissionAspect.java`
- `backend/src/main/java/com/stadium/booking/service/VenueService.java`
- `backend/src/main/java/com/stadium/booking/service/CourtService.java`
- `backend/src/main/java/com/stadium/booking/service/BookingService.java`
- `backend/src/main/java/com/stadium/booking/service/CheckinService.java`
- `backend/src/main/java/com/stadium/booking/service/ViolationService.java`
- `backend/src/main/java/com/stadium/booking/controller/admin/ViolationAdminController.java`
- `backend/src/main/java/com/stadium/booking/controller/admin/UserAdminController.java`
- `docs/superpowers/specs/2026-04-23-venue-manager-role-design.md`
- `docs/superpowers/plans/2026-04-23-venue-manager-role.md`
- `backend/src/main/java/com/stadium/booking/controller/admin/AdminUserAdminController.java`
- `backend/src/main/java/com/stadium/booking/service/AdminUserManagementService.java`
- `backend/src/main/java/com/stadium/booking/service/AdminVenueAccessService.java`
- `backend/src/main/java/com/stadium/booking/repository/AdminRoleRepository.java`
- `backend/src/main/java/com/stadium/booking/repository/VenueStaffRepository.java`
- `backend/src/main/resources/db/migration/V10__update_venue_staff_permissions.sql`
- `admin-web/src/views/admin-user/list.vue`
- `admin-web/src/api/admin-user.js`

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
