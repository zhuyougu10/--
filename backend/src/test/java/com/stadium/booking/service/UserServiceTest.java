package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
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
