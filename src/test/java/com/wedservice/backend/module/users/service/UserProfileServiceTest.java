package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.TravelStyle;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserAddress;
import com.wedservice.backend.module.users.entity.UserDevice;
import com.wedservice.backend.module.users.entity.UserPreference;
import com.wedservice.backend.module.users.entity.UserRole;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserAddressRepository;
import com.wedservice.backend.module.users.repository.UserDeviceRepository;
import com.wedservice.backend.module.users.repository.UserPreferenceRepository;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.mapstruct.factory.Mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserMapper userMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        userProfileService = new UserProfileService(
                userRepository,
                userAddressRepository,
                userDeviceRepository,
                userPreferenceRepository,
                userMapper,
                authenticatedUserProvider
        );
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

    @Test
    void createMyAddress_marksFirstAddressAsDefault_andNormalizesFields() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserAddressRequest request = UserAddressRequest.builder()
                .contactName("  Nguyen Van A  ")
                .contactPhone(" 0901 222 333 ")
                .province("  Da Nang ")
                .district(" Hai Chau ")
                .ward(" ")
                .addressLine(" 123 Tran Phu ")
                .isDefault(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userAddressRepository.existsByUserId(userId)).thenReturn(false);
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> {
            UserAddress address = invocation.getArgument(0);
            address.setId(11L);
            address.setCreatedAt(LocalDateTime.now());
            address.setUpdatedAt(LocalDateTime.now());
            return address;
        });

        UserAddressResponse response = userProfileService.createMyAddress(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getContactName()).isEqualTo("Nguyen Van A");
        assertThat(response.getContactPhone()).isEqualTo("0901222333");
        assertThat(response.getProvince()).isEqualTo("Da Nang");
        assertThat(response.getWard()).isNull();
        assertThat(response.getAddressLine()).isEqualTo("123 Tran Phu");
        assertThat(response.getIsDefault()).isTrue();
        verify(userAddressRepository).clearDefaultByUserId(eq(userId), isNull());
    }

    @Test
    void updateMyAddress_rejectsUnsettingCurrentDefault() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserAddress existingAddress = UserAddress.builder()
                .id(21L)
                .userId(userId)
                .contactName("Current")
                .contactPhone("0901000000")
                .addressLine("Old address")
                .isDefault(true)
                .build();
        UserAddressRequest request = UserAddressRequest.builder()
                .contactName("Updated")
                .contactPhone("0901222333")
                .addressLine("New address")
                .isDefault(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userAddressRepository.findByIdAndUserId(21L, userId)).thenReturn(Optional.of(existingAddress));

        assertThatThrownBy(() -> userProfileService.updateMyAddress(21L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Default address cannot be unset directly. Set another address as default or delete it.");

        verify(userAddressRepository, never()).save(any(UserAddress.class));
    }

    @Test
    void setMyDefaultAddress_switchesDefaultWithinCurrentUserScope() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserAddress existingAddress = UserAddress.builder()
                .id(31L)
                .userId(userId)
                .contactName("Office")
                .contactPhone("0901222333")
                .addressLine("Office address")
                .isDefault(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userAddressRepository.findByIdAndUserId(31L, userId)).thenReturn(Optional.of(existingAddress));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAddressResponse response = userProfileService.setMyDefaultAddress(31L);

        assertThat(response.getIsDefault()).isTrue();
        verify(userAddressRepository).clearDefaultByUserId(userId, 31L);
    }

    @Test
    void deleteMyAddress_promotesNextAddressWhenDeletingDefault() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserAddress defaultAddress = UserAddress.builder()
                .id(41L)
                .userId(userId)
                .contactName("Home")
                .contactPhone("0901222333")
                .addressLine("Home address")
                .isDefault(true)
                .build();
        UserAddress replacementAddress = UserAddress.builder()
                .id(42L)
                .userId(userId)
                .contactName("Office")
                .contactPhone("0901333444")
                .addressLine("Office address")
                .isDefault(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userAddressRepository.findByIdAndUserId(41L, userId)).thenReturn(Optional.of(defaultAddress));
        when(userAddressRepository.findFirstByUserIdOrderByIdAsc(userId)).thenReturn(Optional.of(replacementAddress));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.deleteMyAddress(41L);

        assertThat(replacementAddress.getIsDefault()).isTrue();
        verify(userAddressRepository).delete(defaultAddress);
        verify(userAddressRepository).clearDefaultByUserId(userId, 42L);
        verify(userAddressRepository).save(replacementAddress);
    }

    @Test
    void getMyAddresses_returnsSortedResponsesForCurrentUser() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        LocalDateTime now = LocalDateTime.now();
        UserAddress primaryAddress = UserAddress.builder()
                .id(51L)
                .userId(userId)
                .contactName("Home")
                .contactPhone("0901222333")
                .addressLine("Home")
                .isDefault(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        UserAddress secondaryAddress = UserAddress.builder()
                .id(52L)
                .userId(userId)
                .contactName("Office")
                .contactPhone("0901333444")
                .addressLine("Office")
                .isDefault(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userAddressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId))
                .thenReturn(List.of(primaryAddress, secondaryAddress));

        List<UserAddressResponse> responses = userProfileService.getMyAddresses();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(51L);
        assertThat(responses.get(0).getIsDefault()).isTrue();
        assertThat(responses.get(1).getId()).isEqualTo(52L);
    }

    @Test
    void getMyPreferences_returnsDefaults_whenUserHasNoSavedPreferences() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserPreferenceResponse response = userProfileService.getMyPreferences();

        assertThat(response.getId()).isNull();
        assertThat(response.getFavoriteRegions()).isEmpty();
        assertThat(response.getFavoriteTags()).isEmpty();
        assertThat(response.getFavoriteDestinations()).isEmpty();
        assertThat(response.getPrefersLowMobility()).isFalse();
        assertThat(response.getPrefersWeatherAlert()).isTrue();
        assertThat(response.getPrefersPromotionAlert()).isTrue();
    }

    @Test
    void updateMyPreferences_upsertsAndNormalizesLists() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserPreferenceRequest request = UserPreferenceRequest.builder()
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredTripMode(PreferredTripMode.PRIVATE)
                .travelStyle(TravelStyle.FOOD)
                .preferredDepartureCity("  Ho Chi Minh City ")
                .favoriteRegions(List.of("  North ", "Central", "North", " "))
                .favoriteTags(List.of("food", " culture ", "food"))
                .favoriteDestinations(List.of("Da Nang", " Da Nang ", "Hue"))
                .prefersLowMobility(true)
                .prefersFamilyFriendly(null)
                .prefersStudentBudget(true)
                .prefersWeatherAlert(null)
                .prefersPromotionAlert(false)
                .build();
        UserPreference existingPreference = UserPreference.builder()
                .id(61L)
                .userId(userId)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPreference));
        when(userPreferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> {
            UserPreference preference = invocation.getArgument(0);
            preference.setUpdatedAt(LocalDateTime.now());
            if (preference.getCreatedAt() == null) {
                preference.setCreatedAt(LocalDateTime.now());
            }
            return preference;
        });

        UserPreferenceResponse response = userProfileService.updateMyPreferences(request);

        assertThat(response.getId()).isEqualTo(61L);
        assertThat(response.getBudgetLevel()).isEqualTo(BudgetLevel.MEDIUM);
        assertThat(response.getPreferredTripMode()).isEqualTo(PreferredTripMode.PRIVATE);
        assertThat(response.getTravelStyle()).isEqualTo(TravelStyle.FOOD);
        assertThat(response.getPreferredDepartureCity()).isEqualTo("Ho Chi Minh City");
        assertThat(response.getFavoriteRegions()).containsExactly("North", "Central");
        assertThat(response.getFavoriteTags()).containsExactly("food", "culture");
        assertThat(response.getFavoriteDestinations()).containsExactly("Da Nang", "Hue");
        assertThat(response.getPrefersLowMobility()).isTrue();
        assertThat(response.getPrefersFamilyFriendly()).isFalse();
        assertThat(response.getPrefersStudentBudget()).isTrue();
        assertThat(response.getPrefersWeatherAlert()).isTrue();
        assertThat(response.getPrefersPromotionAlert()).isFalse();
    }

    @Test
    void getMyDevices_returnsOnlyActiveDevicesForCurrentUser() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        LocalDateTime now = LocalDateTime.now();
        UserDevice firstDevice = UserDevice.builder()
                .id(71L)
                .userId(userId)
                .platform("android")
                .deviceName("Pixel 8")
                .pushToken("token-1")
                .appVersion("1.0.0")
                .isActive(true)
                .lastSeenAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        UserDevice secondDevice = UserDevice.builder()
                .id(72L)
                .userId(userId)
                .platform("ios")
                .deviceName("iPhone")
                .pushToken("token-2")
                .appVersion("1.1.0")
                .isActive(true)
                .lastSeenAt(now.minusDays(1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userDeviceRepository.findByUserIdAndIsActiveTrueOrderByLastSeenAtDescIdDesc(userId))
                .thenReturn(List.of(firstDevice, secondDevice));

        List<UserDeviceResponse> responses = userProfileService.getMyDevices();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(71L);
        assertThat(responses.get(0).getPlatform()).isEqualTo("android");
        assertThat(responses.get(1).getId()).isEqualTo(72L);
    }

    @Test
    void registerMyDevice_reusesExistingDeviceByPushToken() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserDeviceRequest request = UserDeviceRequest.builder()
                .platform(" Android ")
                .deviceName(" Pixel 8 ")
                .pushToken(" token-1 ")
                .appVersion(" 1.2.0 ")
                .build();
        UserDevice existingDevice = UserDevice.builder()
                .id(81L)
                .userId(userId)
                .platform("android")
                .deviceName("Old Device")
                .pushToken("token-1")
                .appVersion("1.0.0")
                .isActive(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userDeviceRepository.findFirstByUserIdAndPushToken(userId, "token-1")).thenReturn(Optional.of(existingDevice));
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> {
            UserDevice device = invocation.getArgument(0);
            device.setUpdatedAt(LocalDateTime.now());
            if (device.getCreatedAt() == null) {
                device.setCreatedAt(LocalDateTime.now());
            }
            return device;
        });

        UserDeviceResponse response = userProfileService.registerMyDevice(request);

        assertThat(response.getId()).isEqualTo(81L);
        assertThat(response.getPlatform()).isEqualTo("android");
        assertThat(response.getDeviceName()).isEqualTo("Pixel 8");
        assertThat(response.getPushToken()).isEqualTo("token-1");
        assertThat(response.getAppVersion()).isEqualTo("1.2.0");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getLastSeenAt()).isNotNull();
    }

    @Test
    void registerMyDevice_throwsBadRequest_whenPushTokenAlreadyActive() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserDeviceRequest request = UserDeviceRequest.builder()
                .platform("android")
                .pushToken("token-already-active")
                .build();
        UserDevice activeDevice = UserDevice.builder()
                .id(82L)
                .userId(userId)
                .pushToken("token-already-active")
                .isActive(true)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userDeviceRepository.findFirstByUserIdAndPushToken(userId, "token-already-active"))
                .thenReturn(Optional.of(activeDevice));

        assertThatThrownBy(() -> userProfileService.registerMyDevice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Device with this push token is already registered and active.");

        verify(userDeviceRepository, never()).save(any(UserDevice.class));
    }

    @Test
    void registerMyDevice_rejectsWhenNoDeviceNameAndNoPushToken() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserDeviceRequest request = UserDeviceRequest.builder()
                .platform("web")
                .deviceName(" ")
                .pushToken(" ")
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> userProfileService.registerMyDevice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("At least deviceName or pushToken must be provided");
    }

    @Test
    void deleteMyDevice_marksDeviceInactive() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserDevice device = UserDevice.builder()
                .id(91L)
                .userId(userId)
                .platform("ios")
                .deviceName("iPhone")
                .pushToken("token-9")
                .appVersion( "2.0.0")
                .isActive(true)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userDeviceRepository.findByIdAndUserId(91L, userId)).thenReturn(Optional.of(device));
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.deleteMyDevice(91L);

        assertThat(device.getIsActive()).isFalse();
        verify(userDeviceRepository).save(device);
    }

    @Test
    void deleteMyDevice_throwsBadRequest_whenDeviceAlreadyInactive() {
        UUID userId = UUID.randomUUID();
        User currentUser = activeUser(userId);
        UserDevice device = UserDevice.builder()
                .id(92L)
                .userId(userId)
                .isActive(false)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userDeviceRepository.findByIdAndUserId(92L, userId)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> userProfileService.deleteMyDevice(92L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Device has already been removed or is inactive.");

        verify(userDeviceRepository, never()).save(any(UserDevice.class));
    }

    private User activeUser(UUID userId) {
        User user = User.builder()
                .id(userId)
                .fullName("Current User")
                .email("current@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();

        user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());
        return user;
    }
}
