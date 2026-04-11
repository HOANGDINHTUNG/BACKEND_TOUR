package com.wedservice.backend.module.auth.service.command.impl;

import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.service.command.AuthCommandService;
import com.wedservice.backend.module.auth.validator.AuthValidator;
import com.wedservice.backend.module.users.entity.Gender;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.wedservice.backend.common.util.DataNormalizer;

@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthValidator authValidator;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = DataNormalizer.normalizeEmail(request.getEmail());
        String phone = DataNormalizer.normalizePhone(request.getPhone());
        authValidator.validateRequiredContact(email, phone);
        authValidator.validateUniqueContacts(email, phone, null);

        String fullName = request.getFullName().trim();
        User user = User.builder()
                .fullName(fullName)
                .displayName(StringUtils.hasText(request.getDisplayName()) ? request.getDisplayName().trim() : fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPasswordHash()))
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .gender(request.getGender() == null ? Gender.UNKNOWN : request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .avatarUrl(StringUtils.hasText(request.getAvatarUrl()) ? request.getAvatarUrl().trim() : null)
                .build();

        User savedUser = userRepository.save(user);

        // Build AuthResponse similar to previous AuthService.buildAuthResponse
        return AuthResponse.builder()
                .user(userMapper.toDto(savedUser))
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(com.wedservice.backend.module.auth.security.CustomUserDetails.fromUser(savedUser)))
                .expiresIn(jwtService.getExpiration())
                .build();
    }
}
