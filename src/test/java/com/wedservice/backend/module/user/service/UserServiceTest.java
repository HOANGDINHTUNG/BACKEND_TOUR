package com.wedservice.backend.module.user.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.user.dto.request.CreateUserRequest;
import com.wedservice.backend.module.user.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.user.dto.request.UpdateUserRequest;
import com.wedservice.backend.module.user.dto.response.UserResponse;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.mapper.UserMapper;
import com.wedservice.backend.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        userService = new UserService(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void createUser_normalizesEmail_andEncodesPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFullName(" Nguyen Van A ");
        request.setEmail(" TEST@EXAMPLE.COM ");
        request.setPassword("123456");
        request.setPhone("0987654321");

        User savedUser = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .password("encoded-password")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        verify(userRepository).existsByEmailIgnoreCase("test@example.com");
    }

    @Test
    void updateUser_throwsBadRequest_whenRoleIsInvalid() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("test@example.com");
        request.setPhone("0987654321");
        request.setRole("manager");

        User existingUser = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .password("encoded-password")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Role must be ADMIN or CUSTOMER");
    }

    @Test
    void getUsers_returnsPagedResponse() {
        User user = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .password("encoded")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1);
        when(userRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        PageResponse<UserResponse> response = userService.getUsers(0, 5, null, null);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateMyProfile_throwsUnauthorized_whenUserIdIsNull() {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("test@example.com");
        request.setPhone("0987654321");

        assertThatThrownBy(() -> userService.updateMyProfile(null, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");
    }

    @Test
    void deactivateUser_setsActiveToFalse() {
        User existingUser = User.builder()
                .id(2L)
                .fullName("Nguyen Van B")
                .email("b@example.com")
                .password("encoded")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.deactivateUser(2L);

        assertThat(response.getActive()).isFalse();
        verify(userRepository).save(eq(existingUser));
    }
}
