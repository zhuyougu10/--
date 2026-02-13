# Task 03: 认证授权模块

> **依赖:** task-02-backend-core.md
> **预计时间:** 4-5 小时

## 目标
实现完整的认证授权系统，包括微信小程序登录、后台管理员登录、JWT Token管理、RBAC权限控制。

---

## 模块概览

```
┌─────────────────────────────────────────────────────────┐
│                    认证授权架构                          │
├─────────────────────────────────────────────────────────┤
│  小程序端                    后台管理端                  │
│     │                           │                       │
│     ▼                           ▼                       │
│  微信登录                    账号密码登录                 │
│  (code -> openid)           (username/password)         │
│     │                           │                       │
│     └───────────┬───────────────┘                       │
│                 ▼                                       │
│           JWT Token 生成                                │
│                 │                                       │
│     ┌───────────┴───────────┐                          │
│     ▼                       ▼                          │
│  普通用户                  管理员用户                     │
│  (User表)                (AdminUser表)                  │
│     │                       │                          │
│     ▼                       ▼                          │
│  基础权限                 RBAC权限控制                   │
│                          (Role/Permission)             │
└─────────────────────────────────────────────────────────┘
```

---

## Step 1: 创建 JWT 工具类

**文件:** `backend/src/main/java/com/stadium/booking/security/JwtUtils.java`

```java
package com.stadium.booking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String userType, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("isAdmin", isAdmin);
        return createToken(claims, userId.toString(), expiration);
    }

    public String generateRefreshToken(Long userId) {
        return createToken(new HashMap<>(), userId.toString(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUserTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType", String.class);
    }

    public boolean isAdmin(String token) {
        Claims claims = parseToken(token);
        return claims.get("isAdmin", Boolean.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

---

## Step 2: 创建微信登录服务

**文件:** `backend/src/main/java/com/stadium/booking/service/WechatService.java`

```java
package com.stadium.booking.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WechatService {
    private static final String JSCODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    public WechatSessionResult code2Session(String code) {
        String url = JSCODE2SESSION_URL
            .replace("{appid}", appid)
            .replace("{secret}", secret)
            .replace("{code}", code);

        String response = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(response);

        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.error("Wechat login failed: {}", response);
            throw new RuntimeException("微信登录失败: " + json.getStr("errmsg"));
        }

        WechatSessionResult result = new WechatSessionResult();
        result.setOpenid(json.getStr("openid"));
        result.setSessionKey(json.getStr("session_key"));
        result.setUnionid(json.getStr("unionid"));
        return result;
    }

    @lombok.Data
    public static class WechatSessionResult {
        private String openid;
        private String sessionKey;
        private String unionid;
    }
}
```

---

## Step 3: 创建认证服务

**文件:** `backend/src/main/java/com/stadium/booking/service/AuthService.java`

```java
package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.WechatLoginRequest;
import com.stadium.booking.dto.request.AdminLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final WechatService wechatService;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        WechatService.WechatSessionResult session = wechatService.code2Session(request.getCode());
        String openid = session.getOpenid();

        User user = userRepository.findByOpenid(openid)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setOpenid(openid);
                newUser.setUnionid(session.getUnionid());
                newUser.setUserType(1);
                newUser.setStatus(1);
                newUser.setNoShowCount(0);
                return userRepository.save(newUser);
            });

        if (user.getStatus() == 0) {
            if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_BANNED, 
                    "账号已被禁用至 " + user.getBannedUntil().toLocalDate());
            }
        }

        String token = jwtUtils.generateToken(user.getId(), "USER", false);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setUserType("USER");
        response.setIsNewUser(user.getName() == null);
        return response;
    }

    public LoginResponse adminLogin(AdminLoginRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));

        if (admin.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_BANNED, "账号已被禁用");
        }

        if (!verifyPassword(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        String token = jwtUtils.generateToken(admin.getId(), "ADMIN", true);
        String refreshToken = jwtUtils.generateRefreshToken(admin.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(admin.getId());
        response.setUserType("ADMIN");
        response.setIsNewUser(false);
        return response;
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return cn.hutool.crypto.digest.BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
```

---

## Step 4: 创建认证控制器

**文件:** `backend/src/main/java/com/stadium/booking/controller/api/AuthController.java`

```java
package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.WechatLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证API", description = "小程序端认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "微信登录")
    @PostMapping("/wechat/login")
    public Result<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return Result.success(authService.wechatLogin(request));
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/controller/admin/AdminAuthController.java`

```java
package com.stadium.booking.controller.admin;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.AdminLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台认证API", description = "后台管理端认证接口")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AuthService authService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(authService.adminLogin(request));
    }
}
```

---

## Step 5: 创建 DTO 类

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/WechatLoginRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/request/AdminLoginRequest.java`

```java
package com.stadium.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/dto/response/LoginResponse.java`

```java
package com.stadium.booking.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String userType;
    private Boolean isNewUser;
}
```

---

## Step 6: 创建 Repository

**文件:** `backend/src/main/java/com/stadium/booking/repository/UserRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE openid = #{openid} AND deleted_at IS NULL")
    Optional<User> findByOpenid(String openid);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted_at IS NULL")
    Optional<User> findById(Long id);
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/AdminUserRepository.java`

```java
package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface AdminUserRepository extends BaseMapper<AdminUser> {
    @Select("SELECT * FROM admin_user WHERE username = #{username} AND deleted_at IS NULL")
    Optional<AdminUser> findByUsername(String username);

    @Select("SELECT * FROM admin_user WHERE id = #{id} AND deleted_at IS NULL")
    Optional<AdminUser> findById(Long id);
}
```

---

## Step 7: 创建 Security 配置

**文件:** `backend/src/main/java/com/stadium/booking/security/JwtAuthenticationFilter.java`

```java
package com.stadium.booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String userType = jwtUtils.getUserTypeFromToken(token);
            boolean isAdmin = jwtUtils.isAdmin(token);

            UserPrincipal principal = new UserPrincipal(userId, userType, isAdmin);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    principal, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType))
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/security/UserPrincipal.java`

```java
package com.stadium.booking.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String userType;
    private Boolean isAdmin;
}
```

**文件:** `backend/src/main/java/com/stadium/booking/config/SecurityConfig.java`

```java
package com.stadium.booking.config;

import com.stadium.booking.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/api/auth/**", "/admin/auth/**").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "VENUE_STAFF")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Step 8: 创建权限注解

**文件:** `backend/src/main/java/com/stadium/booking/security/RequirePermission.java`

```java
package com.stadium.booking.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value();
}
```

**文件:** `backend/src/main/java/com/stadium/booking/security/PermissionAspect.java`

```java
package com.stadium.booking.security;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final AdminUserRepository adminUserRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        if (principal.getIsAdmin()) {
            return joinPoint.proceed();
        }

        AdminUser admin = adminUserRepository.findById(principal.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        boolean hasPermission = rolePermissionRepository.hasPermission(admin.getId(), requirePermission.value());
        
        if (!hasPermission) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return joinPoint.proceed();
    }
}
```

**文件:** `backend/src/main/java/com/stadium/booking/repository/RolePermissionRepository.java`

```java
package com.stadium.booking.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RolePermissionRepository {
    @Select("""
        SELECT COUNT(*) > 0 FROM role_permission rp
        JOIN admin_user_role aur ON aur.role_id = rp.role_id
        JOIN permission p ON p.id = rp.permission_id
        WHERE aur.admin_user_id = #{adminUserId} AND p.code = #{permissionCode}
        """)
    boolean hasPermission(Long adminUserId, String permissionCode);
}
```

---

## Step 9: 创建用户上下文工具

**文件:** `backend/src/main/java/com/stadium/booking/security/UserContext.java`

```java
package com.stadium.booking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getUserId();
        }
        return null;
    }

    public static String getCurrentUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getUserType();
        }
        return null;
    }

    public static boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getIsAdmin();
        }
        return false;
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
    }
}
```

---

## Step 10: 创建初始管理员数据

**文件:** `backend/src/main/resources/db/migration/V3__init_admin_data.sql`

```sql
-- 插入默认管理员账号 (密码: admin123)
INSERT INTO admin_user (username, password_hash, name, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1);

-- 分配管理员角色
INSERT INTO admin_user_role (admin_user_id, role_id)
SELECT au.id, r.id FROM admin_user au, role r 
WHERE au.username = 'admin' AND r.code = 'ADMIN';
```

---

## Step 11: 验证认证功能

**测试微信登录:**
```bash
curl -X POST http://localhost:8080/api/api/auth/wechat/login \
  -H "Content-Type: application/json" \
  -d '{"code": "test_code"}'
```

**测试管理员登录:**
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "userType": "ADMIN",
    "isNewUser": false
  }
}
```

---

## 提交

```bash
git add backend/
git commit -m "feat(auth): implement wechat login, admin login and RBAC"
```

---

## 注意事项

1. **密码安全**: 使用 BCrypt 加密，生产环境建议增加密码复杂度校验
2. **Token 刷新**: 实现刷新 Token 机制，避免用户频繁登录
3. **微信 SessionKey**: 不要暴露给前端，仅用于后端解密用户信息
4. **权限缓存**: 生产环境建议将用户权限缓存到 Redis
