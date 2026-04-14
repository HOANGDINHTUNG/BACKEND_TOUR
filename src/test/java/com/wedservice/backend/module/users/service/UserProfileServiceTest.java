package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserRole;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.mapstruct.factory.Mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserMapper userMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        userProfileService = new UserProfileService(userRepository, userMapper, authenticatedUserProvider);
    }

    @Test
    void getMyProfile_returnsCurrentAuthenticatedUser() {
        UUID id = UUID.randomUUID();
        User currentUser = User.builder()
                .id(id)
                .fullName("Current User")
                .email("current@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();
        
        currentUser.getUserRoles().add(UserRole.builder()
                .user(currentUser)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        UserResponse response = userProfileService.getMyProfile();

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEmail()).isEqualTo("current@example.com");
    }

    @Test
    void updateMyProfile_updatesBasicFields_andAuditActor() {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName(" Updated User ");
        request.setEmail(" UPDATED@EXAMPLE.COM ");
        request.setPhone("0911222333");

        UUID id = UUID.randomUUID();
        User currentUser = User.builder()
                .id(id)
                .fullName("Current User")
                .email("current@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();
        
        currentUser.getUserRoles().add(UserRole.builder()
                .user(currentUser)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(id);
        when(authenticatedUserProvider.getRequiredCurrentUserLogin()).thenReturn("current@example.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@example.com", id)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userProfileService.updateMyProfile(request);

        assertThat(response.getFullName()).isEqualTo("Updated User");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(currentUser.getRoleName()).isEqualTo("CUSTOMER");
    }

    @Test
    void updateMyProfile_throwsBadRequest_whenEmailBelongsToAnotherUser() {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("Current User");
        request.setEmail("other@example.com");
        request.setPhone("0987654321");

        UUID id = UUID.randomUUID();
        User currentUser = User.builder()
                .id(id)
                .fullName("Current User")
                .email("current@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();
        
        currentUser.getUserRoles().add(UserRole.builder()
                .user(currentUser)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("other@example.com", id)).thenReturn(true);

        assertThatThrownBy(() -> userProfileService.updateMyProfile(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void getMyProfile_throwsUnauthorized_whenUserIsSuspended() {
        UUID id = UUID.randomUUID();
        User suspendedUser = User.builder()
                .id(id)
                .fullName("Suspended User")
                .status(Status.SUSPENDED)
                .build();
        
        suspendedUser.getUserRoles().add(UserRole.builder()
                .user(suspendedUser)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(suspendedUser));

        assertThatThrownBy(() -> userProfileService.getMyProfile())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Your account is suspended");
    }

    @Test
    void getMyProfile_throwsUnauthorized_whenNoAuthenticatedUserExists() {
        when(authenticatedUserProvider.getRequiredCurrentUserId())
                .thenThrow(new UnauthorizedException("Unauthorized"));

        assertThatThrownBy(() -> userProfileService.getMyProfile())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");
    }
}
