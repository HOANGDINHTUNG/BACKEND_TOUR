package com.wedservice.backend.module.users.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProfileValidator {

    private final UserRepository userRepository;

    public void validateRequiredContact(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BadRequestException("At least email or phone must be provided");
        }
    }

    public void validateUniqueContacts(String email, String phone, UUID currentUserId) {
        if (StringUtils.hasText(email) && userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId)) {
            throw new BadRequestException("Email already exists");
        }
        if (StringUtils.hasText(phone) && userRepository.existsByPhoneAndIdNot(phone, currentUserId)) {
            throw new BadRequestException("Phone already exists");
        }
    }
}
