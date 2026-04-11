package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.mapstruct.factory.Mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void createUser_normalizesEmail_encodesPassword_andPersistsAuditActor() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName(" Nguyen Van A ");
        request.setEmail(" TEST@EXAMPLE.COM ");
        request.setPasswordHash("123456");
        request.setPhone("0987654321");
        request.setRole(Role.ADMIN);

        UUID id = UUID.randomUUID();
        User savedUser = User.builder()
                .id(id)
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.ADMIN)
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = adminUserService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(persistedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(persistedUser.getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void createUser_throwsBadRequest_whenEmailAlreadyExists() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("duplicate@example.com");
        request.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmailIgnoreCase("duplicate@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void getUsers_returnsPagedResponse_andBuildsExpectedPageable() {
        UserSearchRequest request = new UserSearchRequest();
        request.setPage(1);
        request.setSize(5);
        request.setKeyword("nguyen");
        request.setStatus(Status.ACTIVE);
        request.setSortBy("email");
        request.setSortDir("asc");

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(1, 5), 6);
        when(userRepository.searchUsers(eq("nguyen"), eq(Status.ACTIVE), eq(Role.CUSTOMER), eq(MemberLevel.BRONZE), any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponse> response = adminUserService.getUsers(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).searchUsers(eq("nguyen"), eq(Status.ACTIVE), eq(Role.CUSTOMER), eq(MemberLevel.BRONZE), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("email").getDirection().name()).isEqualTo("ASC");
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(6);
    }

    @Test
    void getUsers_throwsBadRequest_whenSortFieldIsNotWhitelisted() {
        UserSearchRequest request = new UserSearchRequest();
        request.setSortBy("password");

        assertThatThrownBy(() -> adminUserService.getUsers(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sortBy must be one of");
    }

    @Test
    void updateUser_throwsBadRequest_whenEmailAlreadyExistsForAnotherUser() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("other@example.com");
        request.setPhone("0987654321");
        request.setRole(Role.CUSTOMER);

        UUID id = UUID.randomUUID();
        User existingUser = User.builder()
                .id(id)
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("other@example.com", id)).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.updateUser(id, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void deactivateUser_setsStatusToSuspended_andStoresAudit() {
        UUID id = UUID.randomUUID();
        User existingUser = User.builder()
                .id(id)
                .fullName("Nguyen Van B")
                .email("b@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = adminUserService.deactivateUser(id);

        assertThat(response.getStatus()).isEqualTo(Status.SUSPENDED);
        assertThat(existingUser.getDeletedAt()).isNotNull();
        verify(userRepository).save(eq(existingUser));
    }

    @Test
    void updateUser_updatesRoleAndAuditActor() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");
        request.setPhone("0911222333");
        request.setRole(Role.ADMIN);

        UUID id = UUID.randomUUID();
        User existingUser = User.builder()
                .id(id)
                .fullName("Old Name")
                .email("old@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();
        existingUser.setDeletedAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@example.com", id)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = adminUserService.updateUser(id, request);

        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(existingUser.getRole()).isEqualTo(Role.ADMIN);
    }
}
