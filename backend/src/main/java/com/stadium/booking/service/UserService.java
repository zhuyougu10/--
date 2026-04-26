package com.stadium.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.UserProfileUpdateRequest;
import com.stadium.booking.dto.request.BindStudentNoRequest;
import com.stadium.booking.dto.request.UserPresetCreateRequest;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.entity.User;
import com.stadium.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public IPage<UserResponse> listPage(Integer current, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(User::getName, keyword)
                .or()
                .like(User::getPhone, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> page = userRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toResponse);
    }

    @Transactional
    public UserResponse createPresetUser(UserPresetCreateRequest request) {
        String name = normalizeRequired(request.getName(), "姓名不能为空");
        String studentNo = normalizeRequired(request.getStudentNo(), "工号/学号不能为空");
        String phone = normalizeOptional(request.getPhone());

        if (request.getUserType() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户类型不能为空");
        }
        if (request.getUserType() < 1 || request.getUserType() > 3) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户类型非法");
        }
        if (userRepository.findByStudentNo(studentNo).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "工号/学号已存在");
        }
        if (phone != null && userRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "手机号已存在");
        }

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setStudentNo(studentNo);
        user.setUserType(request.getUserType());
        user.setStatus(1);
        user.setIsBound(0);
        user.setNoShowCount(0);
        try {
            userRepository.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "工号/学号已存在");
        }
        return getUserDetail(user.getId());
    }

    public UserResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        return toResponse(user);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setStatus(status);
        if (status == 1) {
            user.setBannedUntil(null);
        }
        userRepository.updateById(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        userRepository.updateById(user);
        return getUserDetail(userId);
    }

    @Transactional
    public UserResponse bindStudentNo(Long userId, BindStudentNoRequest request) {
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        if (currentUser.getIsBound() != null && currentUser.getIsBound() == 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "您已绑定工号/学号，无需重复绑定");
        }

        User presetUser = userRepository.findUnboundByStudentNo(request.getStudentNo())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "工号/学号不存在或已被绑定"));

        if (!presetUser.getName().equals(request.getName())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "姓名与工号/学号不匹配");
        }

        presetUser.setOpenid(currentUser.getOpenid());
        presetUser.setUnionId(currentUser.getUnionId());
        if (currentUser.getPhone() != null) {
            presetUser.setPhone(currentUser.getPhone());
        }
        if (currentUser.getAvatar() != null) {
            presetUser.setAvatar(currentUser.getAvatar());
        }
        presetUser.setIsBound(1);
        presetUser.setBoundAt(LocalDateTime.now());

        if (presetUser.getId() != null && !presetUser.getId().equals(userId)) {
            userRepository.hardDeleteById(userId);
        }

        userRepository.updateById(presetUser);

        return getUserDetail(presetUser.getId());
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setStudentNo(user.getStudentNo());
        response.setUserType(user.getUserType());
        response.setUserTypeText(getUserTypeText(user.getUserType()));
        response.setStatus(user.getStatus());
        response.setStatusText(getStatusText(user.getStatus()));
        response.setNoShowCount(user.getNoShowCount());
        response.setLastNoShowAt(user.getLastNoShowAt());
        response.setBannedUntil(user.getBannedUntil());
        response.setIsBound(user.getIsBound());
        response.setCreatedAt(user.getCreatedAt());
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

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "禁用";
            case 1 -> "正常";
            default -> "未知";
        };
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
