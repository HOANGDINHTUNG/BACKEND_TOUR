package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.entity.AuditLog;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.Permission;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.RoleScope;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.TravelStyle;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserRole;
import com.wedservice.backend.module.users.repository.AuditLogRepository;
import com.wedservice.backend.module.users.repository.PermissionRepository;
import com.wedservice.backend.module.users.repository.RoleRepository;
import com.wedservice.backend.module.users.repository.UserAddressRepository;
import com.wedservice.backend.module.users.repository.UserDeviceRepository;
import com.wedservice.backend.module.users.repository.UserPreferenceRepository;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Phase2UserAdminIntegrationTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AdminRbacCommandService adminRbacCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        seedCurrentUser(currentUserId, "current-phase2@example.com", "CUSTOMER");
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);
    }

    @Test
    void addressCrudAndDefaultSwitching_arePersistedForCurrentUser() {
        UserAddressResponse firstAddress = userProfileService.createMyAddress(UserAddressRequest.builder()
                .contactName(" Nguyen Van A ")
                .contactPhone(" 0901 111 111 ")
                .province(" Da Nang ")
                .district(" Hai Chau ")
                .addressLine(" 123 Tran Phu ")
                .isDefault(false)
                .build());

        UserAddressResponse secondAddress = userProfileService.createMyAddress(UserAddressRequest.builder()
                .contactName(" Nguyen Van B ")
                .contactPhone(" 0902 222 222 ")
                .province(" Quang Nam ")
                .district(" Hoi An ")
                .addressLine(" 456 Nguyen Hue ")
                .isDefault(false)
                .build());

        UserAddressResponse updatedSecond = userProfileService.updateMyAddress(secondAddress.getId(), UserAddressRequest.builder()
                .contactName(" Nguyen Van B Updated ")
                .contactPhone(" 0902 222 999 ")
                .province(" Quang Nam ")
                .district(" Hoi An ")
                .ward(" Cam Pho ")
                .addressLine(" 789 Bach Dang ")
                .isDefault(true)
                .build());

        List<UserAddressResponse> addressesAfterSwitch = userProfileService.getMyAddresses();
        assertThat(firstAddress.getIsDefault()).isTrue();
        assertThat(secondAddress.getIsDefault()).isFalse();
        assertThat(updatedSecond.getIsDefault()).isTrue();
        assertThat(updatedSecond.getContactName()).isEqualTo("Nguyen Van B Updated");
        assertThat(updatedSecond.getContactPhone()).isEqualTo("0902222999");
        assertThat(addressesAfterSwitch).extracting(UserAddressResponse::getId)
                .containsExactly(updatedSecond.getId(), firstAddress.getId());

        userProfileService.deleteMyAddress(updatedSecond.getId());

        List<UserAddressResponse> addressesAfterDelete = userProfileService.getMyAddresses();
        assertThat(addressesAfterDelete).hasSize(1);
        assertThat(addressesAfterDelete.get(0).getId()).isEqualTo(firstAddress.getId());
        assertThat(addressesAfterDelete.get(0).getIsDefault()).isTrue();
        assertThat(userAddressRepository.findByUserIdOrderByIsDefaultDescIdAsc(currentUserId)).hasSize(1);
    }

    @Test
    void preferenceUpsert_persistsNormalizedListsAndDefaults() {
        UserPreferenceResponse response = userProfileService.updateMyPreferences(UserPreferenceRequest.builder()
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredTripMode(PreferredTripMode.PRIVATE)
                .travelStyle(TravelStyle.FOOD)
                .preferredDepartureCity(" Ho Chi Minh City ")
                .favoriteRegions(List.of(" North ", "Central", "North"))
                .favoriteTags(List.of("food", " culture ", "food"))
                .favoriteDestinations(List.of("Da Nang", " Da Nang ", "Hue"))
                .prefersLowMobility(true)
                .prefersFamilyFriendly(null)
                .prefersStudentBudget(true)
                .prefersWeatherAlert(null)
                .prefersPromotionAlert(false)
                .build());

        UserPreferenceResponse fetched = userProfileService.getMyPreferences();

        assertThat(response.getBudgetLevel()).isEqualTo(BudgetLevel.MEDIUM);
        assertThat(response.getPreferredTripMode()).isEqualTo(PreferredTripMode.PRIVATE);
        assertThat(response.getTravelStyle()).isEqualTo(TravelStyle.FOOD);
        assertThat(response.getPreferredDepartureCity()).isEqualTo("Ho Chi Minh City");
        assertThat(response.getFavoriteRegions()).containsExactly("North", "Central");
        assertThat(response.getFavoriteTags()).containsExactly("food", "culture");
        assertThat(response.getFavoriteDestinations()).containsExactly("Da Nang", "Hue");
        assertThat(response.getPrefersFamilyFriendly()).isFalse();
        assertThat(response.getPrefersWeatherAlert()).isTrue();
        assertThat(fetched.getFavoriteDestinations()).containsExactly("Da Nang", "Hue");
        assertThat(userPreferenceRepository.findByUserId(currentUserId)).isPresent();
    }

    @Test
    void deviceRegisterAndRemove_softDeletesAndHidesInactiveDevice() {
        UserDeviceResponse registered = userProfileService.registerMyDevice(UserDeviceRequest.builder()
                .platform(" Android ")
                .deviceName(" Pixel 8 ")
                .pushToken(" token-phase2 ")
                .appVersion(" 1.2.3 ")
                .build());

        List<UserDeviceResponse> activeDevices = userProfileService.getMyDevices();
        assertThat(registered.getPlatform()).isEqualTo("android");
        assertThat(registered.getDeviceName()).isEqualTo("Pixel 8");
        assertThat(registered.getPushToken()).isEqualTo("token-phase2");
        assertThat(activeDevices).hasSize(1);

        userProfileService.deleteMyDevice(registered.getId());

        List<UserDeviceResponse> afterDelete = userProfileService.getMyDevices();
        assertThat(afterDelete).isEmpty();
        assertThat(userDeviceRepository.findById(registered.getId())).get()
                .extracting(device -> device.getIsActive())
                .isEqualTo(false);
    }

    @Test
    void rolePermissionUpdate_createsAuditLogWithActorAndNewState() {
        Permission roleView = permissionRepository.save(Permission.builder()
                .code("role.view")
                .name("View Role")
                .moduleName("role")
                .actionName("view")
                .isActive(true)
                .build());
        Permission permissionView = permissionRepository.save(Permission.builder()
                .code("permission.view")
                .name("View Permission")
                .moduleName("permission")
                .actionName("view")
                .isActive(true)
                .build());
        Role role = roleRepository.save(Role.builder()
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .description("Backoffice audit role")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(40)
                .isSystemRole(false)
                .isActive(true)
                .build());

        RoleResponse response = adminRbacCommandService.updateRolePermissions(role.getId(), UpdateRolePermissionsRequest.builder()
                .permissionCodes(List.of(" role.view ", "permission.view"))
                .build());

        Role reloadedRole = roleRepository.findWithPermissionsById(role.getId()).orElseThrow();
        AuditLog auditLog = auditLogRepository.findAll().stream()
                .filter(log -> "permission.assign".equals(log.getActionName()))
                .findFirst()
                .orElseThrow();

        assertThat(response.getPermissions()).extracting(permission -> permission.getCode())
                .containsExactly("permission.view", "role.view");
        assertThat(reloadedRole.getPermissions()).extracting(Permission::getId)
                .containsExactlyInAnyOrder(roleView.getId(), permissionView.getId());
        assertThat(auditLog.getActorUserId()).isEqualTo(currentUserId);
        assertThat(auditLog.getEntityName()).isEqualTo("roles");
        assertThat(auditLog.getEntityId()).isEqualTo(String.valueOf(role.getId()));
        assertThat(auditLog.getNewData()).contains("permission.view");
        assertThat(auditLog.getOldData()).isNotNull();
    }

    private void seedCurrentUser(UUID userId, String email, String roleCode) {
        Role role = roleRepository.save(Role.builder()
                .code(roleCode)
                .name(roleCode + " role")
                .roleScope(RoleScope.CUSTOMER)
                .hierarchyLevel(10)
                .isSystemRole(false)
                .isActive(true)
                .build());

        User user = User.builder()
                .id(userId)
                .fullName("Phase 2 User")
                .email(email)
                .passwordHash("encoded-password")
                .phone("0909000000")
                .status(Status.ACTIVE)
                .build();
        user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(role)
                .isPrimary(true)
                .build());
        userRepository.save(user);
    }
}
