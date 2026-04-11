package com.wedservice.backend.module.auth.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.auth.security.JwtService;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.mapstruct.factory.Mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, userMapper);
    }

    @Test
    void register_createsUser_andReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(" Nguyen Van A ");
        request.setEmail(" TEST@EXAMPLE.COM ");
        request.setPasswordHash("123456");
        request.setPhone("0987654321");

        UUID id = UUID.randomUUID();
        User savedUser = User.builder()
                .id(id)
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(86_400_000L);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(persistedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(persistedUser.getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void register_throwsBadRequest_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        LoginRequest request = new LoginRequest();
        request.setLogin(" TEST@EXAMPLE.COM ");
        request.setPasswordHash("123456");

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .fullName("Nguyen Van B")
                .email("test@example.com")
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.ADMIN)
                .build();

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(86_400_000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getUser().getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findById(eq(id));
    }

    @Test
    void login_throwsNotFound_whenAuthenticatedUserCannotBeLoadedFromDatabase() {
        LoginRequest request = new LoginRequest();
        request.setLogin("test@example.com");
        request.setPasswordHash("123456");

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .fullName("Ghost")
                .email("test@example.com")
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + id);
    }
}
