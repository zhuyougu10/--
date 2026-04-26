package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.BindStudentNoRequest;
import com.stadium.booking.dto.request.UserPresetCreateRequest;
import com.stadium.booking.dto.response.UserResponse;
import com.stadium.booking.entity.User;
import com.stadium.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createPresetUserCreatesUnboundRecord() {
        UserPresetCreateRequest request = new UserPresetCreateRequest();
        request.setName("张三");
        request.setPhone("13800000009");
        request.setStudentNo("20260001");
        request.setUserType(1);

        when(userRepository.findByStudentNo("20260001")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("13800000009")).thenReturn(Optional.empty());
        when(userRepository.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(8L);
            return 1;
        });
        when(userRepository.findById(8L)).thenReturn(Optional.of(buildUser()));

        UserResponse response = userService.createPresetUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("张三", saved.getName());
        assertEquals("20260001", saved.getStudentNo());
        assertEquals(1, saved.getStatus());
        assertEquals(0, saved.getIsBound());
        assertNull(saved.getOpenid());
        assertNull(saved.getUnionId());
        assertEquals(0, response.getIsBound());
    }

    @Test
    void createPresetUserRejectsDuplicateStudentNo() {
        UserPresetCreateRequest request = new UserPresetCreateRequest();
        request.setName("张三");
        request.setStudentNo("20260001");
        request.setUserType(1);

        when(userRepository.findByStudentNo("20260001")).thenReturn(Optional.of(new User()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.createPresetUser(request));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    void bindStudentNoMergesWechatIdentityIntoPresetUser() {
        BindStudentNoRequest request = new BindStudentNoRequest();
        request.setStudentNo("2021004");
        request.setName("张三");

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setOpenid("openid-1");
        currentUser.setUnionId("union-1");
        currentUser.setPhone("13800000011");
        currentUser.setAvatar("https://avatar.example.com/a.png");
        currentUser.setIsBound(0);

        User presetUser = new User();
        presetUser.setId(2L);
        presetUser.setStudentNo("2021004");
        presetUser.setName("张三");
        presetUser.setUserType(1);
        presetUser.setStatus(1);
        presetUser.setIsBound(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findUnboundByStudentNo("2021004")).thenReturn(Optional.of(presetUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(presetUser));

        userService.bindStudentNo(1L, request);

        verify(userRepository, never()).updateById(currentUser);
        verify(userRepository).updateById(presetUser);
        verify(userRepository).hardDeleteById(1L);

        var inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(1L);
        inOrder.verify(userRepository).findUnboundByStudentNo("2021004");
        inOrder.verify(userRepository).hardDeleteById(1L);
        inOrder.verify(userRepository).updateById(presetUser);
    }

    private User buildUser() {
        User user = new User();
        user.setId(8L);
        user.setName("张三");
        user.setPhone("13800000009");
        user.setStudentNo("20260001");
        user.setUserType(1);
        user.setStatus(1);
        user.setIsBound(0);
        return user;
    }
}
