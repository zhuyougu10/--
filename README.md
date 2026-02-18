# 校园球馆智能预约系统

一个完整的校园球馆预约管理系统，包含后端服务、管理后台和微信小程序三个子项目。

## 项目结构

```
├── backend/          # Spring Boot 后端服务
├── admin-web/        # Vue 3 管理后台
├── miniapp/          # uni-app 微信小程序
└── docs/             # 项目文档
```

## 技术栈

| 模块 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.2.0 + Java 17 + MySQL + Redis + MyBatis Plus |
| 管理后台 | Vue 3 + Vite + Ant Design Vue + Pinia |
| 小程序 | uni-app + Vue 3 + TypeScript |

---

## 环境要求

### 必需环境

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 后端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存服务 |
| Node.js | 18+ | 前端构建环境 |
| Maven | 3.8+ | Java 依赖管理 |

### 开发工具

| 工具 | 用途 |
|------|------|
| IntelliJ IDEA | 后端开发 |
| HBuilderX | 小程序开发 |
| 微信开发者工具 | 小程序预览与调试 |

---

## 一、后端服务 (backend)

### 1.1 环境准备

#### 安装 JDK 17

**Windows:**
1. 下载 [JDK 17](https://adoptium.net/temurin/releases/?version=17)
2. 安装后配置环境变量：
   - `JAVA_HOME`: JDK 安装目录
   - `Path`: 添加 `%JAVA_HOME%\bin`
3. 验证安装：
   ```bash
   java -version
   ```

#### 安装 MySQL 8.0

1. 下载并安装 [MySQL 8.0](https://dev.mysql.com/downloads/mysql/)
2. 创建数据库：
   ```sql
   CREATE DATABASE stadium_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

#### 安装 Redis

**Windows:**
1. 下载 [Redis for Windows](https://github.com/tporadowski/redis/releases)
2. 解压后运行 `redis-server.exe`

**验证 Redis 连接：**
```bash
redis-cli ping
# 返回 PONG 表示成功
```

### 1.2 IntelliJ IDEA 配置

#### 安装 IDEA

1. 下载 [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (推荐 Ultimate 版)
2. 安装完成后启动 IDEA

#### 导入项目

1. 打开 IDEA，选择 `File` → `Open`
2. 选择项目根目录下的 `backend` 文件夹
3. 等待 Maven 自动导入依赖（首次可能需要几分钟）

#### 配置 JDK

1. `File` → `Project Structure` → `Project`
2. SDK 选择 JDK 17
3. Language level 选择 17

#### 安装推荐插件

1. `File` → `Settings` → `Plugins`
2. 搜索并安装：
   - **Lombok**: 简化 Java 代码
   - **MyBatisX**: MyBatis 开发增强
   - **Spring Boot Helper**: Spring Boot 开发支持

### 1.3 配置文件修改

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stadium_booking?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的 MySQL 用户名
    password: root          # 修改为你的 MySQL 密码
  data:
    redis:
      host: localhost       # Redis 地址
      port: 6379            # Redis 端口

jwt:
  secret: your-256-bit-secret-key-here-must-be-at-least-32-characters

wechat:
  appid: 你的微信小程序AppID
  secret: 你的微信小程序AppSecret
```

### 1.4 启动后端服务

#### 方式一：IDEA 启动（推荐开发环境）

1. 找到 `src/main/java/com/stadium/booking/BookingApplication.java`
2. 右键 → `Run 'BookingApplication'`
3. 控制台显示 `Started BookingApplication` 表示启动成功
4. 访问 http://localhost:8080/api 验证服务

#### 方式二：命令行启动

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

### 1.5 数据库初始化

项目使用 Flyway 进行数据库版本管理，启动时会自动执行迁移脚本：

- `V1__init_schema.sql`: 创建基础表结构
- `V3__init_admin_data.sql`: 初始化管理员账号

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

---

## 二、管理后台 (admin-web)

### 2.1 环境准备

#### 安装 Node.js

1. 下载 [Node.js LTS 版本](https://nodejs.org/) (推荐 18.x 或 20.x)
2. 安装时勾选 "Automatically install the necessary tools"
3. 验证安装：
   ```bash
   node -v
   npm -v
   ```

### 2.2 安装依赖

```bash
cd admin-web
npm install
```

### 2.3 配置后端地址

编辑 `admin-web/src/utils/request.js`，确认后端地址：

```javascript
const baseURL = 'http://localhost:8080/api'
```

### 2.4 启动开发服务器

```bash
npm run dev
```

启动成功后访问 http://localhost:5173

### 2.5 构建生产版本

```bash
npm run build
```

构建产物在 `dist` 目录下。

---

## 三、微信小程序 (miniapp)

### 3.1 环境准备

#### 安装 HBuilderX

1. 下载 [HBuilderX](https://www.dcloud.io/hbuilderx.html) (推荐 App 开发版)
2. 解压后直接运行 `HBuilderX.exe`

#### 安装微信开发者工具

1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 安装完成后登录微信账号

### 3.2 HBuilderX 配置

#### 导入项目

1. 打开 HBuilderX
2. `文件` → `导入` → `从本地目录导入`
3. 选择项目根目录下的 `miniapp` 文件夹

#### 配置微信开发者工具路径

1. `工具` → `设置` → `运行配置`
2. 找到 "微信开发者工具路径"
3. 设置为微信开发者工具的安装路径，例如：
   - Windows: `C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat`

#### 安装依赖

在 HBuilderX 内置终端中执行：

```bash
cd miniapp
npm install
```

### 3.3 微信开发者工具配置

#### 开启服务端口

1. 打开微信开发者工具
2. `设置` → `安全设置`
3. 开启 "服务端口"

#### 登录微信账号

使用微信扫码登录开发者工具

### 3.4 配置小程序 AppID

编辑 `miniapp/src/manifest.json`：

```json
{
  "mp-weixin": {
    "appid": "你的微信小程序AppID"
  }
}
```

或在 HBuilderX 中：
1. 打开 `manifest.json`
2. 点击 "微信小程序配置"
3. 填写 AppID

### 3.5 配置后端地址

编辑 `miniapp/src/utils/request.js`：

```javascript
const baseURL = 'http://localhost:8080/api'
```

**注意：** 小程序要求使用 HTTPS，开发阶段需要：
1. 在微信开发者工具中勾选 "不校验合法域名"
2. 或使用内网穿透工具配置 HTTPS

### 3.6 运行小程序

#### 方式一：HBuilderX 运行（推荐）

1. 在 HBuilderX 中打开项目
2. `运行` → `运行到小程序模拟器` → `微信开发者工具`
3. 等待编译完成，微信开发者工具会自动打开

#### 方式二：命令行编译

```bash
cd miniapp
npm run dev:mp-weixin
```

编译产物在 `dist/dev/mp-weixin` 目录，用微信开发者工具打开此目录。

### 3.7 构建生产版本

```bash
npm run build:mp-weixin
```

构建产物在 `dist/build/mp-weixin` 目录。

---

## 四、完整启动流程

### 开发环境启动顺序

1. **启动 MySQL 和 Redis**
   ```bash
   # 确保 MySQL 服务运行中
   redis-server
   ```

2. **启动后端服务** (IDEA)
   - 运行 `BookingApplication.java`
   - 等待启动完成，访问 http://localhost:8080/api

3. **启动管理后台**
   ```bash
   cd admin-web
   npm run dev
   ```
   - 访问 http://localhost:5173

4. **启动小程序**
   - HBuilderX 运行到微信开发者工具

---

## 五、常见问题排查

### 后端问题

#### 1. 端口被占用

**错误信息：** `Port 8080 was already in use`

**解决方案：**
```bash
# Windows 查找占用进程
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F
```

或修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081
```

#### 2. 数据库连接失败

**错误信息：** `Communications link failure`

**排查步骤：**
1. 确认 MySQL 服务已启动
2. 检查数据库连接配置是否正确
3. 确认数据库 `stadium_booking` 已创建
4. 检查防火墙是否阻止连接

#### 3. Redis 连接失败

**错误信息：** `Unable to connect to Redis`

**解决方案：**
1. 确认 Redis 服务已启动
2. 检查 Redis 配置地址和端口
3. 如果设置了密码，在配置中添加：
   ```yaml
   spring:
     data:
       redis:
         password: 你的密码
   ```

#### 4. Maven 依赖下载失败

**解决方案：**
1. 检查网络连接
2. 配置国内镜像源，编辑 Maven 的 `settings.xml`：
   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

### 前端问题

#### 1. npm install 失败

**解决方案：**
```bash
# 清除缓存
npm cache clean --force

# 使用国内镜像
npm install --registry=https://registry.npmmirror.com
```

#### 2. Vite 启动报错

**错误信息：** `Cannot find module...`

**解决方案：**
```bash
# 删除 node_modules 重新安装
rm -rf node_modules package-lock.json
npm install
```

### 小程序问题

#### 1. 编译报错 "AppID 未配置"

**解决方案：**
1. 在 `manifest.json` 中配置 AppID
2. 或在微信开发者工具中使用测试号

#### 2. 请求失败 "request:fail"

**解决方案：**
1. 微信开发者工具中：`设置` → `项目设置` → 勾选 "不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
2. 确认后端服务已启动
3. 检查请求地址是否正确

#### 3. HBuilderX 无法唤起微信开发者工具

**解决方案：**
1. 确认微信开发者工具已开启服务端口
2. 检查 HBuilderX 中的工具路径配置
3. 尝试手动在微信开发者工具中导入编译后的项目

#### 4. 真机预览白屏

**解决方案：**
1. 确认已配置合法的 HTTPS 域名
2. 检查是否有 JavaScript 错误
3. 确认接口返回数据格式正确

---

## 六、开发建议

### 推荐开发流程

1. 先启动后端服务，确保数据库和 Redis 正常
2. 使用管理后台进行数据管理和测试
3. 最后在小程序中进行完整功能测试

### 代码规范

- 后端遵循阿里巴巴 Java 开发手册
- 前端使用 ESLint + Prettier 格式化
- 提交代码前确保通过所有测试

### 相关文档

- [需求文档](docs/requirements.md)
- [API 一致性报告](docs/api-consistency-report.md)
- [任务计划](docs/plans/)

---

## 许可证

MIT License
