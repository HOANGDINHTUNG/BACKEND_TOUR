package com.wedservice.backend.module.auth.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final UserRepository userRepository;

    public void validateRequiredContact(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BadRequestException("At least email or phone must be provided");
        }
    }

    public void validateUniqueContacts(String email, String phone, java.util.UUID currentUserId) {
        if (StringUtils.hasText(email)) {
            boolean emailExists = currentUserId == null
                    ? userRepository.existsByEmailIgnoreCase(email)
                    : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);
            if (emailExists) throw new BadRequestException("Email already exists");
        }

        if (StringUtils.hasText(phone)) {
            boolean phoneExists = currentUserId == null
                    ? userRepository.existsByPhone(phone)
                    : userRepository.existsByPhoneAndIdNot(phone, currentUserId);
            if (phoneExists) throw new BadRequestException("Phone already exists");
        }
    }
}
