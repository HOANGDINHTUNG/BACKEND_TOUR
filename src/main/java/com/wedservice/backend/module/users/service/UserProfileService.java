package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserAddress;
import com.wedservice.backend.module.users.entity.UserDevice;
import com.wedservice.backend.module.users.entity.UserPreference;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserAddressRepository;
import com.wedservice.backend.module.users.repository.UserDeviceRepository;
import com.wedservice.backend.module.users.repository.UserPreferenceRepository;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        return userMapper.toDto(findCurrentUser());
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        User currentUser = findCurrentUser();
        String email = DataNormalizer.normalizeEmail(request.getEmail());
        String phone = DataNormalizer.normalizePhone(request.getPhone());
        validateRequiredContact(email, phone);
        validateUniqueContacts(email, phone, currentUser.getId());

        userMapper.applyProfileUpdate(currentUser, request, email, phone);

        User updatedUser = userRepository.save(currentUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> getMyAddresses() {
        UUID userId = findCurrentUser().getId();
        return userAddressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId)
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse getMyPreferences() {
        UUID userId = findCurrentUser().getId();
        return userPreferenceRepository.findByUserId(userId)
                .map(this::toPreferenceResponse)
                .orElseGet(this::defaultPreferenceResponse);
    }

    @Transactional(readOnly = true)
    public List<UserDeviceResponse> getMyDevices() {
        UUID userId = findCurrentUser().getId();
        return userDeviceRepository.findByUserIdAndIsActiveTrueOrderByLastSeenAtDescIdDesc(userId)
                .stream()
                .map(this::toDeviceResponse)
                .toList();
    }

    @Transactional
    public UserAddressResponse createMyAddress(UserAddressRequest request) {
        UUID userId = findCurrentUser().getId();

        boolean hasExistingAddress = userAddressRepository.existsByUserId(userId);
        boolean shouldBeDefault = !hasExistingAddress || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            userAddressRepository.clearDefaultByUserId(userId, null);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .isDefault(shouldBeDefault)
                .build();
        applyAddressRequest(address, request);

        return toAddressResponse(userAddressRepository.save(address));
    }

    @Transactional
    public UserAddressResponse updateMyAddress(Long id, UserAddressRequest request) {
        UUID userId = findCurrentUser().getId();
        UserAddress address = findMyAddress(userId, id);
        boolean isCurrentlyDefault = Boolean.TRUE.equals(address.getIsDefault());
        Boolean requestedDefault = request.getIsDefault();

        if (Boolean.FALSE.equals(requestedDefault) && isCurrentlyDefault) {
            throw new BadRequestException("Default address cannot be unset directly. Set another address as default or delete it.");
        }

        if (Boolean.TRUE.equals(requestedDefault)) {
            userAddressRepository.clearDefaultByUserId(userId, id);
            address.setIsDefault(true);
        }

        applyAddressRequest(address, request);
        return toAddressResponse(userAddressRepository.save(address));
    }

    @Transactional
    public UserAddressResponse setMyDefaultAddress(Long id) {
        UUID userId = findCurrentUser().getId();
        UserAddress address = findMyAddress(userId, id);

        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            userAddressRepository.clearDefaultByUserId(userId, id);
            address.setIsDefault(true);
            address = userAddressRepository.save(address);
        }

        return toAddressResponse(address);
    }

    @Transactional
    public void deleteMyAddress(Long id) {
        UUID userId = findCurrentUser().getId();
        UserAddress address = findMyAddress(userId, id);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        userAddressRepository.delete(address);

        if (wasDefault) {
            userAddressRepository.findFirstByUserIdOrderByIdAsc(userId)
                    .ifPresent(nextAddress -> {
                        userAddressRepository.clearDefaultByUserId(userId, nextAddress.getId());
                        nextAddress.setIsDefault(true);
                        userAddressRepository.save(nextAddress);
                    });
        }
    }

    @Transactional
    public UserPreferenceResponse updateMyPreferences(UserPreferenceRequest request) {
        UUID userId = findCurrentUser().getId();
        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder()
                        .userId(userId)
                        .build());

        applyPreferenceRequest(preference, request);
        return toPreferenceResponse(userPreferenceRepository.save(preference));
    }

    @Transactional
    public UserDeviceResponse registerMyDevice(UserDeviceRequest request) {
        UUID userId = findCurrentUser().getId();
        String normalizedPushToken = normalizeNullable(request.getPushToken());
        String normalizedDeviceName = normalizeNullable(request.getDeviceName());

        if (!StringUtils.hasText(normalizedPushToken) && !StringUtils.hasText(normalizedDeviceName)) {
            throw new BadRequestException("At least deviceName or pushToken must be provided");
        }

        UserDevice device = StringUtils.hasText(normalizedPushToken)
                ? userDeviceRepository.findFirstByUserIdAndPushToken(userId, normalizedPushToken)
                    .orElseGet(() -> UserDevice.builder().userId(userId).build())
                : UserDevice.builder().userId(userId).build();

        applyDeviceRequest(device, request, normalizedDeviceName, normalizedPushToken);
        device.setIsActive(true);
        device.setLastSeenAt(java.time.LocalDateTime.now());

        return toDeviceResponse(userDeviceRepository.save(device));
    }

    @Transactional
    public void deleteMyDevice(Long id) {
        UUID userId = findCurrentUser().getId();
        UserDevice device = findMyDevice(userId, id);
        if (!Boolean.TRUE.equals(device.getIsActive())) {
            return;
        }
        device.setIsActive(false);
        userDeviceRepository.save(device);
    }

    private User findCurrentUser() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != com.wedservice.backend.module.users.entity.Status.ACTIVE) {
            throw new com.wedservice.backend.common.exception.UnauthorizedException("Your account is " + user.getStatus().getValue() + ". Please contact support.");
        }

        return user;
    }

    private UserAddress findMyAddress(UUID userId, Long addressId) {
        return userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }

    private UserDevice findMyDevice(UUID userId, Long deviceId) {
        return userDeviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
    }

    private void applyAddressRequest(UserAddress address, UserAddressRequest request) {
        address.setContactName(DataNormalizer.normalize(request.getContactName()));
        address.setContactPhone(DataNormalizer.normalizePhone(request.getContactPhone()));
        address.setProvince(normalizeNullable(request.getProvince()));
        address.setDistrict(normalizeNullable(request.getDistrict()));
        address.setWard(normalizeNullable(request.getWard()));
        address.setAddressLine(DataNormalizer.normalize(request.getAddressLine()));
    }

    private void applyPreferenceRequest(UserPreference preference, UserPreferenceRequest request) {
        preference.setBudgetLevel(request.getBudgetLevel());
        preference.setPreferredTripMode(request.getPreferredTripMode());
        preference.setTravelStyle(request.getTravelStyle());
        preference.setPreferredDepartureCity(normalizeNullable(request.getPreferredDepartureCity()));
        preference.setFavoriteRegions(normalizeStringList(request.getFavoriteRegions()));
        preference.setFavoriteTags(normalizeStringList(request.getFavoriteTags()));
        preference.setFavoriteDestinations(normalizeStringList(request.getFavoriteDestinations()));
        preference.setPrefersLowMobility(defaultBoolean(request.getPrefersLowMobility(), false));
        preference.setPrefersFamilyFriendly(defaultBoolean(request.getPrefersFamilyFriendly(), false));
        preference.setPrefersStudentBudget(defaultBoolean(request.getPrefersStudentBudget(), false));
        preference.setPrefersWeatherAlert(defaultBoolean(request.getPrefersWeatherAlert(), true));
        preference.setPrefersPromotionAlert(defaultBoolean(request.getPrefersPromotionAlert(), true));
    }

    private void applyDeviceRequest(
            UserDevice device,
            UserDeviceRequest request,
            String normalizedDeviceName,
            String normalizedPushToken
    ) {
        String platform = normalizeNullable(request.getPlatform());
        if (!StringUtils.hasText(platform)) {
            throw new BadRequestException("platform is required");
        }

        device.setPlatform(platform.toLowerCase(java.util.Locale.ROOT));
        device.setDeviceName(normalizedDeviceName);
        device.setPushToken(normalizedPushToken);
        device.setAppVersion(normalizeNullable(request.getAppVersion()));
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String item = DataNormalizer.normalize(value);
            if (StringUtils.hasText(item)) {
                normalized.add(item);
            }
        }
        return new ArrayList<>(normalized);
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private UserAddressResponse toAddressResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .addressLine(address.getAddressLine())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    private UserPreferenceResponse toPreferenceResponse(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .budgetLevel(preference.getBudgetLevel())
                .preferredTripMode(preference.getPreferredTripMode())
                .travelStyle(preference.getTravelStyle())
                .preferredDepartureCity(preference.getPreferredDepartureCity())
                .favoriteRegions(copyList(preference.getFavoriteRegions()))
                .favoriteTags(copyList(preference.getFavoriteTags()))
                .favoriteDestinations(copyList(preference.getFavoriteDestinations()))
                .prefersLowMobility(preference.getPrefersLowMobility())
                .prefersFamilyFriendly(preference.getPrefersFamilyFriendly())
                .prefersStudentBudget(preference.getPrefersStudentBudget())
                .prefersWeatherAlert(preference.getPrefersWeatherAlert())
                .prefersPromotionAlert(preference.getPrefersPromotionAlert())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    private UserPreferenceResponse defaultPreferenceResponse() {
        return UserPreferenceResponse.builder()
                .favoriteRegions(List.of())
                .favoriteTags(List.of())
                .favoriteDestinations(List.of())
                .prefersLowMobility(false)
                .prefersFamilyFriendly(false)
                .prefersStudentBudget(false)
                .prefersWeatherAlert(true)
                .prefersPromotionAlert(true)
                .build();
    }

    private UserDeviceResponse toDeviceResponse(UserDevice device) {
        return UserDeviceResponse.builder()
                .id(device.getId())
                .platform(device.getPlatform())
                .deviceName(device.getDeviceName())
                .pushToken(device.getPushToken())
                .appVersion(device.getAppVersion())
                .isActive(device.getIsActive())
                .lastSeenAt(device.getLastSeenAt())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    private List<String> copyList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private void validateRequiredContact(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BadRequestException("At least email or phone must be provided");
        }
    }

    private void validateUniqueContacts(String email, String phone, UUID currentUserId) {
        if (StringUtils.hasText(email) && userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId)) {
            throw new BadRequestException("Email already exists");
        }

        if (StringUtils.hasText(phone) && userRepository.existsByPhoneAndIdNot(phone, currentUserId)) {
            throw new BadRequestException("Phone already exists");
        }
    }
}
