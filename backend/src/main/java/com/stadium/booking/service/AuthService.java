package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.WechatLoginRequest;
import com.stadium.booking.dto.request.AdminLoginRequest;
import com.stadium.booking.dto.response.LoginResponse;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.AdminUser;
import com.stadium.booking.repository.AdminRoleRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.AdminUserRepository;
import com.stadium.booking.repository.VenueStaffRepository;
import com.stadium.booking.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final WechatService wechatService;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final VenueStaffRepository venueStaffRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        WechatService.WechatSessionResult session = wechatService.code2Session(request.getCode());
        String openid = session.getOpenid();

        User user = userRepository.findByOpenid(openid).orElse(null);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionId(session.getUnionid());
            user.setStatus(1);
            user.setNoShowCount(0);
            userRepository.insert(user);
        }

        if (user.getStatus() == 0) {
            if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_BANNED, 
                    "账号已被禁用至 " + user.getBannedUntil().toLocalDate());
            }
        }

        String token = jwtUtils.generateToken(user.getId(), "USER", false);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        boolean isBound = user.getIsBound() != null && user.getIsBound() == 1;
        boolean needBind = !isBound;

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setUserType(user.getUserType());
        response.setUserTypeText(getUserTypeText(user.getUserType()));
        response.setIsNewUser(user.getName() == null);
        response.setIsBound(isBound);
        response.setNeedBind(needBind);
        return response;
    }

    private String getUserTypeText(Integer type) {
        if (type == null) return "未绑定";
        return switch (type) {
            case 1 -> "学生";
            case 2 -> "教师";
            case 3 -> "外部人员";
            default -> "未知";
        };
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
        adminUserRepository.updateById(admin);

        String role = resolveAdminRole(admin.getId());
        boolean isAdmin = "ADMIN".equals(role);

        String token = jwtUtils.generateToken(admin.getId(), role, isAdmin);
        String refreshToken = jwtUtils.generateRefreshToken(admin.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setName(admin.getName());
        response.setRole(role);
        response.setRoleText(getAdminRoleText(role));
        response.setUserType(0);
        response.setUserTypeText(getAdminRoleText(role));
        response.setIsNewUser(false);
        response.setIsBound(true);
        response.setNeedBind(false);
        return response;
    }

    public LoginResponse getAdminProfile(Long adminId) {
        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        String role = resolveAdminRole(adminId);

        LoginResponse response = new LoginResponse();
        response.setUserId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setName(admin.getName());
        response.setRole(role);
        response.setRoleText(getAdminRoleText(role));
        response.setUserType(0);
        response.setUserTypeText(getAdminRoleText(role));
        response.setIsNewUser(false);
        response.setIsBound(true);
        response.setNeedBind(false);
        return response;
    }

    private String resolveAdminRole(Long adminId) {
        List<String> roleCodes = adminRoleRepository.findRoleCodesByAdminUserId(adminId);
        if (roleCodes.contains("ADMIN")) {
            return "ADMIN";
        }
        if (roleCodes.contains("VENUE_STAFF")) {
            if (venueStaffRepository.findVenueIdsByAdminUserId(adminId).isEmpty()) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号未分配可管理球馆");
            }
            return "VENUE_STAFF";
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号未分配后台角色");
    }

    private String getAdminRoleText(String role) {
        return switch (role) {
            case "ADMIN" -> "管理员";
            case "VENUE_STAFF" -> "场馆管理员";
            default -> "未知角色";
        };
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return cn.hutool.crypto.digest.BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
