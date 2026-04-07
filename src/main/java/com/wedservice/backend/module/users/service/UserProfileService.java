package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.util.UserContactNormalizer;

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
        return userMapper.toResponse(findCurrentUser());
    }

    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        User currentUser = findCurrentUser();
        String email = UserContactNormalizer.normalizeEmail(request.getEmail());
        String phone = UserContactNormalizer.normalizePhone(request.getPhone());
        validateRequiredContact(email, phone);
        validateUniqueContacts(email, phone, currentUser.getId());

        userMapper.applyProfileUpdate(currentUser, request, email, phone);

        User updatedUser = userRepository.save(currentUser);
        return userMapper.toResponse(updatedUser);
    }

    private User findCurrentUser() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
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
