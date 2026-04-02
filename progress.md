# Progress Log

## Session: 2026-02-13

### Phase 1: 目录结构创建
- **Status:** complete
- **Started:** 2026-02-13
- Actions taken:
  - 创建 docs/plans 目录
  - 确认目录结构正确
- Files created/modified:
  - docs/plans/ (目录)

### Phase 2: 主计划文件创建
- **Status:** complete
- Actions taken:
  - 创建主实施计划文件
  - 定义任务文件索引
  - 设置执行顺序
  - 定义验收标准
- Files created/modified:
  - docs/plans/2026-02-13-stadium-booking-system.md (创建)

### Phase 3: 后端任务文件创建
- **Status:** complete
- Actions taken:
  - 创建数据库设计任务 (task-01-database-design.md)
  - 创建后端核心框架任务 (task-02-backend-core.md)
  - 创建认证授权模块任务 (task-03-auth-module.md)
  - 创建球馆场地管理任务 (task-04-venue-management.md)
- Files created/modified:
  - docs/plans/task-01-database-design.md
  - docs/plans/task-02-backend-core.md
  - docs/plans/task-03-auth-module.md
  - docs/plans/task-04-venue-management.md

### Phase 4: 预约系统任务文件创建
- **Status:** complete
- Actions taken:
  - 创建预约核心功能任务 (task-05-booking-core.md)
  - 创建智能推荐任务 (task-06-smart-recommendation.md)
  - 创建二维码核销任务 (task-07-qr-checkin.md)
  - 创建违约限制系统任务 (task-08-violation-system.md)
- Files created/modified:
  - docs/plans/task-05-booking-core.md
  - docs/plans/task-06-smart-recommendation.md
  - docs/plans/task-07-qr-checkin.md
  - docs/plans/task-08-violation-system.md

### Phase 5: 前端任务文件创建
- **Status:** complete
- Actions taken:
  - 创建小程序端任务 (task-09-miniapp.md)
  - 创建后台管理端任务 (task-10-admin-web.md)
- Files created/modified:
  - docs/plans/task-09-miniapp.md
  - docs/plans/task-10-admin-web.md

### Phase 6: 文档更新
- **Status:** complete
- Actions taken:
  - 更新 task_plan.md
  - 更新 progress.md
- Files created/modified:
  - task_plan.md
  - progress.md

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| 目录结构 | ls docs/plans | 11个文件 | 11个文件 | ✓ |
| 文件内容 | 读取各任务文件 | 完整内容 | 完整内容 | ✓ |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-02-13 | 目录已存在 | 1 | 忽略，继续执行 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 5 - 完成 |
| Where am I going? | 所有任务已完成 |
| What's the goal? | 创建完整的实施计划文档 |
| What have I learned? | 需求文档已完整分解为10个独立任务 |
| What have I done? | 创建了1个主计划文件和10个任务文件 |

---
*任务完成于 2026-02-13*

## Session: 2026-04-02

### Phase 1: 管理端预约查询异常定位
- **Status:** complete
- **Started:** 2026-04-02
- Actions taken:
  - 读取用户提供的后端异常日志
  - 检查 `BookingRepository` SQL 与 `BookingService` 查询路径
  - 对照 `booking` 表建表语句，确认缺少 `deleted_at` 字段
- Files created/modified:
  - task_plan.md (更新)
  - findings.md (更新)

### Phase 2: 修复方案实施
- **Status:** complete
- Actions taken:
  - 确定采用 Flyway 新增版本迁移修复，不回改历史迁移
  - 准备新增 `booking.deleted_at` 字段迁移脚本
- Files created/modified:
  - backend/src/main/resources/db/migration/V8__add_deleted_at_to_booking.sql (创建)

### Phase 3: 验证与收尾
- **Status:** complete
- Actions taken:
  - 执行 `mvn -q -DskipTests compile`，后端编译通过
  - 完成计划文件与发现文件回写
  - 处理 Flyway `V8` 版本冲突：删除本地 `V8`，改用 `V9` 新迁移
  - 执行数据库修复：将 `flyway_schema_history` 中版本 8 标记为 `DELETE`
  - 执行 `mvn --% spring-boot:run ...` 启动验证，Flyway 校验通过并成功迁移到 `v9`
- Files created/modified:
  - task_plan.md (更新)
  - findings.md (更新)
  - progress.md (更新)
  - backend/src/main/resources/db/migration/V9__add_deleted_at_to_booking.sql (创建)

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-04-02 20:36 | Unknown column 'deleted_at' in 'field list' | 1 | 根因锁定为 booking 表缺字段，采用新增迁移补齐 |
| 2026-04-02 20:44 | Flyway migration version 8 checksum mismatch | 1 | 发现数据库已应用历史 V8，改用 V9 迁移避免冲突 |
| 2026-04-02 20:52 | Detected applied migration not resolved locally: 8 | 1 | 按 Flyway repair 语义将版本 8 记录标记为 DELETE，随后启动通过 |

### Phase 4: 小程序时段接口 OOM 修复
- **Status:** complete
- Actions taken:
  - 分析 `/venues/{venueId}/courts/{courtId}/slots` 调用链与 `TimeSlotService`
  - 结合数据库配置确认 `open_time=00:00`、`close_time=23:59`、`slot_minutes=60`
  - 修复 `generateTimeSlots` 在午夜回绕导致循环无法终止的问题
  - 执行 `mvn -q -DskipTests compile` 编译验证通过
- Files created/modified:
  - backend/src/main/java/com/stadium/booking/service/TimeSlotService.java (更新)

## Error Log (追加)
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-04-02 21:32 | Handler dispatch failed: OutOfMemoryError: Java heap space | 1 | 识别为时段循环午夜回绕导致结果集无限增长，增加前进性保护并中断异常循环 |

### Phase 5: 球馆图片上传与展示
- **Status:** complete
- Actions taken:
  - 新增后台上传接口 `POST /admin/files/upload`，限制格式 jpg/jpeg/png/webp，大小 5MB
  - 新增静态资源映射 `/uploads/**` 与 `/api/uploads/**`
  - 管理端球馆表单新增图片上传、URL 自动回填与预览
  - 小程序端新增图片地址解析工具并接入球馆列表/详情/首页组件
  - 执行 `mvn -q -DskipTests compile` 与 `admin-web npm run build` 验证
- Files created/modified:
  - backend/src/main/java/com/stadium/booking/controller/admin/FileAdminController.java (创建)
  - backend/src/main/java/com/stadium/booking/dto/response/FileUploadResponse.java (创建)
  - backend/src/main/java/com/stadium/booking/config/WebMvcConfig.java (创建)
  - backend/src/main/java/com/stadium/booking/config/SecurityConfig.java (更新)
  - admin-web/src/api/venue.js (更新)
  - admin-web/src/views/venue/form.vue (更新)
  - miniapp/src/utils/asset.ts (创建)
  - miniapp/src/pages/index/index.vue (更新)
  - miniapp/src/pages/venue/venue.vue (更新)
  - miniapp/src/pages/venue-detail/venue-detail.vue (更新)
  - miniapp/src/components/venue-card/venue-card.vue (更新)

### Phase 6: 图片静态访问 500 修复
- **Status:** complete
- Actions taken:
  - 根据报错 `No static resource venue/...` 与实际文件路径排查上传目录
  - 发现文件保存于项目根 `uploads/venue`，但静态映射解析到 `backend/uploads`
  - 统一上传保存路径与静态映射路径，均改为项目根 `uploads`
  - 执行 `mvn -q -DskipTests compile` 验证通过
- Files created/modified:
  - backend/src/main/java/com/stadium/booking/config/WebMvcConfig.java (更新)
  - backend/src/main/java/com/stadium/booking/controller/admin/FileAdminController.java (更新)

## Error Log (再次追加)
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-04-02 22:17 | GET /api/uploads/... 500 No static resource | 1 | 修复上传目录与资源映射目录不一致，统一到项目根 uploads |

### Phase 7: 管理端扫码核销与移动端登录适配
- **Status:** complete
- Actions taken:
  - 新增设备识别工具 `isMobileDevice`
  - 登录成功后移动端自动跳转 `/checkin`
  - 路由守卫补充已登录访问 `/login` 的移动端跳转策略
  - 扫码页接入浏览器相机扫码（BarcodeDetector + getUserMedia）
  - 扫码不可用时降级到手动输入（支持核销码/预约单号）
  - 登录页与扫码页完成移动端响应式优化
  - 执行 `admin-web npm run build` 构建验证通过
- Files created/modified:
  - admin-web/src/utils/device.js (创建)
  - admin-web/src/views/checkin/index.vue (更新)
  - admin-web/src/views/login/index.vue (更新)
  - admin-web/src/store/modules/user.js (更新)
  - admin-web/src/router/index.js (更新)

### Phase 8: 扫码无响应兼容性修复
- **Status:** complete
- Actions taken:
  - 分析无响应场景，定位为部分移动浏览器 `BarcodeDetector` 不可用或识别不稳定
  - 引入 `jsqr` 作为视频帧识别兜底方案，保留原生识别优先策略
  - 扫码循环改为“原生识别优先 + jsqr fallback”双通道
  - 执行 `admin-web npm run build` 构建验证通过
- Files created/modified:
  - admin-web/src/views/checkin/index.vue (更新)
  - admin-web/package.json (依赖变更)
  - admin-web/package-lock.json (依赖变更)

### Phase 9: 小程序预约详情二维码与状态联动优化
- **Status:** complete
- Actions taken:
  - 预约详情页改为进入即生成二维码，减少用户手动操作
  - 新增过期状态展示逻辑：显示红色“已过期”，并隐藏二维码与刷新入口
  - 核销成功后在详情页自动刷新预约状态，展示“已完成”
  - “我的”页面待使用红点计算排除已过期记录，避免误提示
  - 小程序二维码从外链生成切换为本地 Canvas 生成，规避网络不可达问题
- Files created/modified:
  - miniapp/src/pages/booking-detail/booking-detail.vue (更新)
  - miniapp/src/composables/useBooking.ts (更新)
  - miniapp/src/pages/my/my.vue (更新)
  - miniapp/src/pages/my-bookings/my-bookings.vue (更新)
  - miniapp/src/types/booking.ts (更新)
  - miniapp/package.json (依赖变更)
  - miniapp/package-lock.json (依赖变更)

### Phase 10: 管理端 HTTPS 与移动端核销链路补强
- **Status:** complete
- Actions taken:
  - 增加本地 HTTPS 证书生成脚本与 Vite HTTPS 开发配置
  - README 增补局域网 HTTPS、证书信任与 TLS 常见问题排障说明
  - 管理端移动端登录后定向到核销页，布局调整为单页核销优先
- Files created/modified:
  - admin-web/scripts/generate-cert.mjs (创建)
  - admin-web/.cert/dev-cert.pem (创建)
  - admin-web/.cert/dev-key.pem (创建)
  - admin-web/vite.config.js (更新)
  - README.md (更新)

## Error Log (继续追加)
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-04-03 00:xx | 小程序外链二维码加载失败 `ERR_CONNECTION_CLOSED` | 1 | 放弃外链服务，改为本地 Canvas 渲染二维码 |
| 2026-04-03 00:xx | `qrcode` 库报错 `You need to specify a canvas element` | 1 | 改用 `qrcode-generator + uni.createCanvasContext` |
| 2026-04-03 01:xx | 用户反馈二维码仍不可扫 | 1 | 已持续优化渲染参数，后续需真机联调确认最终可扫稳定性 |
