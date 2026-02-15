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
                newUser.setUnionId(session.getUnionid());
                newUser.setUserType(1);
                newUser.setStatus(1);
                newUser.setNoShowCount(0);
                userRepository.insert(newUser);
                return newUser;
            });

        if (user.getStatus() == 0) {
            if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_BANNED, 
                    "账号已被禁用至 " + user.getBannedUntil().toLocalDate());
            }
        }

        String token = jwtUtils.generateToken(user.getId(), "USER", false);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        boolean isBound = user.getIsBound() != null && user.getIsBound() == 1;
        boolean needBind = !isBound && user.getStudentNo() == null;

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setUserType("USER");
        response.setIsNewUser(user.getName() == null);
        response.setIsBound(isBound);
        response.setNeedBind(needBind);
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
        adminUserRepository.updateById(admin);

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
