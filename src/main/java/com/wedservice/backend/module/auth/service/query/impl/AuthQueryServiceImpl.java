package com.wedservice.backend.module.auth.service.query.impl;

import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RefreshTokenRequest;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.auth.security.JwtService;
import com.wedservice.backend.module.auth.service.query.AuthQueryService;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthQueryServiceImpl implements AuthQueryService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String login = DataNormalizer.normalizeLoginIdentifier(request.getLogin());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, request.getPasswordHash())
        );

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails principal)) {
            throw new IllegalArgumentException("Invalid authentication principal");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + principal.getUserId()));

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        return AuthResponse.builder()
                .user(userMapper.toDto(user))
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(userDetails))
                .expiresIn(jwtService.getExpiration())
                .refreshToken(jwtService.generateRefreshToken(userDetails))
                .refreshExpiresIn(jwtService.getRefreshExpiration())
                .build();
    }

    /**
     * Dùng refresh token để lấy access token mới (token rotation).
     * Mỗi lần refresh đều tạo lại cả access token lẫn refresh token mới.
     */
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        String subject = jwtService.extractSubject(refreshToken);
        User user = userRepository.findByLoginIdentifier(subject)
                .orElseThrow(() -> new UnauthorizedException("User not found for refresh token"));

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        return AuthResponse.builder()
                .user(userMapper.toDto(user))
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(userDetails))
                .expiresIn(jwtService.getExpiration())
                .refreshToken(jwtService.generateRefreshToken(userDetails))   // token rotation
                .refreshExpiresIn(jwtService.getRefreshExpiration())
                .build();
    }
}
