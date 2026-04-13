package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.common.util.DataNormalizer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UserResponse getMyProfile() {
        return userMapper.toDto(findCurrentUser());
    }

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

    private User findCurrentUser() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != com.wedservice.backend.module.users.entity.Status.ACTIVE) {
            throw new com.wedservice.backend.common.exception.UnauthorizedException("Your account is " + user.getStatus().getValue() + ". Please contact support.");
        }

        return user;
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
