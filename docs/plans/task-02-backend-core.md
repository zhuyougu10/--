# Task 02: 后端核心框架搭建

> **依赖:** task-01-database-design.md
> **预计时间:** 3-4 小时

## 目标
搭建 Spring Boot 3 后端项目基础框架，包括项目结构、配置管理、通用组件、异常处理、API规范等。

---

## 项目结构

```
backend/
├── src/main/java/com/stadium/booking/
│   ├── BookingApplication.java
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── WebMvcConfig.java
│   │   ├── RedisConfig.java
│   │   └── MyBatisConfig.java
│   ├── controller/             # 控制器
│   │   ├── api/               # 小程序API
│   │   └── admin/             # 后台管理API
│   ├── service/               # 业务服务
│   │   └── impl/
│   ├── repository/            # 数据访问
│   ├── entity/                # 实体类
│   ├── dto/                   # 数据传输对象
│   │   ├── request/
│   │   └── response/
│   ├── vo/                    # 视图对象
│   ├── common/                # 通用组件
│   │   ├── exception/
│   │   ├── result/
│   │   ├── enums/
│   │   └── utils/
│   └── security/              # 安全相关
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
└── pom.xml
```

---

## Step 1: 创建项目并配置 pom.xml

**文件:** `backend/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.stadium</groupId>
    <artifactId>booking</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>stadium-booking</name>
    <description>校园球馆智能预约系统</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.12.3</jjwt.version>
        <hutool.version>5.8.24</hutool.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Tools -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**验证命令:**
```bash
cd backend && mvn dependency:resolve
```

---

## Step 2: 创建应用配置文件

**文件:** `backend/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: stadium-booking
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

server:
  port: 8080
  servlet:
    context-path: /api

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.stadium.booking.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deletedAt
      logic-delete-value: 'NOW()'
      logic-not-delete-value: 'NULL'
```

**文件:** `backend/src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stadium_booking?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 10000

jwt:
  secret: your-256-bit-secret-key-here-must-be-at-least-32-characters
  expiration: 86400000
  refresh-expiration: 604800000

wechat:
  appid: your_wechat_appid
  secret: your_wechat_secret

logging:
  level:
    com.stadium.booking: DEBUG
    org.springframework.security: DEBUG
```

---

## Step 3: 创建主应用类

**文件:** `backend/src/main/java/com/stadium/booking/BookingApplication.java`

```java
package com.stadium.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@MapperScan("com.stadium.booking.repository")
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
}
```

---

## Step 4: 创建通用响应结构

**文件:** `backend/src/main/java/com/stadium/booking/common/result/Result.java`

```java
package com.stadium.booking.common.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private String errorCode;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getHttpStatus());
        result.setMessage(errorCode.getMessage());
        result.setErrorCode(errorCode.getCode());
        return result;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/common/result/ErrorCode.java`

```java
package com.stadium.booking.common.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SLOT_CONFLICT(409, "SLOT_CONFLICT", "时段已被占用"),
    OUT_OF_OPEN_HOURS(400, "OUT_OF_OPEN_HOURS", "不在营业时间"),
    OUT_OF_BOOKING_WINDOW(400, "OUT_OF_BOOKING_WINDOW", "超出可预约窗口"),
    LIMIT_EXCEEDED(403, "LIMIT_EXCEEDED", "超过个人限额"),
    CANCEL_NOT_ALLOWED(403, "CANCEL_NOT_ALLOWED", "已超过取消截止时间"),
    CHECKIN_NOT_ALLOWED(403, "CHECKIN_NOT_ALLOWED", "不在核销窗口"),
    INVALID_REQUEST(400, "INVALID_REQUEST", "参数非法"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "未登录"),
    FORBIDDEN(403, "FORBIDDEN", "无权限"),
    NOT_FOUND(404, "NOT_FOUND", "资源不存在"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "服务内部错误"),
    USER_BANNED(403, "USER_BANNED", "账号已被禁用");

    private final Integer httpStatus;
    private final String code;
    private final String message;
}
```

---

## Step 5: 创建业务异常类

**文件:** `backend/src/main/java/com/stadium/booking/common/exception/BusinessException.java`

```java
package com.stadium.booking.common.exception;

import com.stadium.booking.common.result.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/common/exception/GlobalExceptionHandler.java`

```java
package com.stadium.booking.common.exception;

import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(ErrorCode.INVALID_REQUEST.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return Result.error(ErrorCode.INVALID_REQUEST.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }
}
```

---

## Step 6: 创建枚举类

**文件:** `backend/src/main/java/com/stadium/booking/common/enums/BookingStatus.java`

```java
package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    CONFIRMED(1, "已确认"),
    CANCELLED(2, "已取消"),
    CHECKED_IN(3, "已签到"),
    NO_SHOW(4, "爽约");

    private final Integer code;
    private final String desc;

    public static BookingStatus fromCode(Integer code) {
        for (BookingStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/common/enums/UserType.java`

```java
package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum UserType {
    STUDENT(1, "学生"),
    TEACHER(2, "教师"),
    EXTERNAL(3, "外部人员");

    private final Integer code;
    private final String desc;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/common/enums/SportType.java`

```java
package com.stadium.booking.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum SportType {
    BADMINTON("badminton", "羽毛球"),
    BASKETBALL("basketball", "篮球"),
    TABLE_TENNIS("table_tennis", "乒乓球"),
    TENNIS("tennis", "网球"),
    VOLLEYBALL("volleyball", "排球"),
    FOOTBALL("football", "足球");

    private final String code;
    private final String desc;
}
```

---

## Step 7: 创建基础实体类

**文件:** `backend/src/main/java/com/stadium/booking/entity/BaseEntity.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic(value = "NULL", delval = "NOW()")
    private LocalDateTime deletedAt;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/entity/User.java`

```java
package com.stadium.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String openid;
    private String unionId;
    private String name;
    private String phone;
    private String studentNo;
    private Integer userType;
    private String avatar;
    private Integer status;
    private LocalDateTime bannedUntil;
    private Integer noShowCount;
    private LocalDateTime lastNoShowAt;
}
```

---

## Step 8: 创建 MyBatis-Plus 自动填充

**文件:** `backend/src/main/java/com/stadium/booking/config/MyBatisMetaObjectHandler.java`

```java
package com.stadium.booking.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

---

## Step 9: 创建分页工具

**文件:** `backend/src/main/java/com/stadium/booking/common/result/PageResult.java`

```java
package com.stadium.booking.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private List<T> records;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;

    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        return result;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/PageRequest.java`

```java
package com.stadium.booking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;

@Data
public class PageRequest {
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer size = 10;
}
```

---

## Step 10: 创建健康检查接口

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/HealthController.java`

```java
package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
```

---

## Step 11: 验证项目启动

**验证命令:**
```bash
cd backend && mvn spring-boot:run
```

**预期输出:**
```
Started BookingApplication in X.XXX seconds
```

**测试健康检查:**
```bash
curl http://localhost:8080/api/health
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": 1234567890123
  },
  "timestamp": 1234567890123
}
```

---

## 提交

```bash
git add backend/
git commit -m "feat(backend): init spring boot project with core framework"
```

---

## 注意事项

1. **配置安全**: 生产环境配置不要提交到代码库，使用环境变量或配置中心
2. **JWT密钥**: 生产环境必须使用强密钥，至少256位
3. **数据库连接**: 生产环境建议使用连接池配置优化
4. **日志级别**: 生产环境调整日志级别为 INFO 或 WARN
