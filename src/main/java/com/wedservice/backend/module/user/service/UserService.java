package com.wedservice.backend.module.user.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.user.dto.request.CreateUserRequest;
import com.wedservice.backend.module.user.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.user.dto.request.UpdateUserRequest;
import com.wedservice.backend.module.user.dto.response.UserResponse;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.mapper.UserMapper;
import com.wedservice.backend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse createUser(CreateUserRequest request) {
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
        return userMapper.toResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);
        String email = normalizeEmail(request.getEmail());
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(email);

        if (emailChanged && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already exists");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setRole(parseRole(request.getRole()));

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    public UserResponse deactivateUser(Long id) {
        User user = findUserById(id);
        user.setActive(false);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    public PageResponse<UserResponse> getUsers(int page, int size, String keyword, Boolean active) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasActive = active != null;

        if (hasKeyword && hasActive) {
            userPage = userRepository.findByFullNameContainingIgnoreCaseAndActive(keyword.trim(), active, pageable);
        } else if (hasKeyword) {
            userPage = userRepository.findByFullNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (hasActive) {
            userPage = userRepository.findByActive(active, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream().map(userMapper::toResponse).toList())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    public UserResponse getMyProfile(Long userId) {
        return userMapper.toResponse(findAuthenticatedUser(userId));
    }

    public UserResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        User user = findAuthenticatedUser(userId);
        String email = normalizeEmail(request.getEmail());
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(email);

        if (emailChanged && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already exists");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    private User findAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return findUserById(userId);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Role must be ADMIN or CUSTOMER");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
