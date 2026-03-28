package com.wedservice.backend.module.auth.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.auth.security.JwtService;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.mapper.UserMapper;
import com.wedservice.backend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone().trim())
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getUserId()));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(userDetails))
                .expiresIn(jwtService.getExpiration())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
