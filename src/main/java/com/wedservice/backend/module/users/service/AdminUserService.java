package com.wedservice.backend.module.users.service;

import com.querydsl.core.BooleanBuilder;
import com.wedservice.backend.module.users.entity.QUser;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.mapper.UserMapper;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.repository.RoleRepository;
import com.wedservice.backend.module.users.entity.UserRole;
import com.wedservice.backend.common.util.DataNormalizer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

import com.wedservice.backend.common.service.BaseService;
import org.springframework.data.jpa.repository.JpaRepository;

@Service
@RequiredArgsConstructor
public class AdminUserService extends BaseService<User, UUID> {
    
    @Override
    protected JpaRepository<User, UUID> getRepository() {
        return userRepository;
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "fullName",
            "displayName",
            "email",
            "phone",
            "userCategory",
            "status",
            "memberLevel",
            "createdAt",
            "updatedAt",
            "lastLoginAt",
            "deletedAt"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditTrailRecorder auditTrailRecorder;

    public UserResponse createUser(AdminCreateUserRequest request) {
        String email = DataNormalizer.normalizeEmail(request.getEmail());
        String phone = DataNormalizer.normalizePhone(request.getPhone());
        validateRequiredContact(email, phone);
        validateUniqueContacts(email, phone, null);

        User user = userMapper.toNewUser(
                request,
                email,
                phone,
                passwordEncoder.encode(request.getPasswordHash())
        );

        User savedUser = userRepository.save(user);

        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            java.util.List<com.wedservice.backend.module.users.entity.Role> roles = roleRepository.findAllByCodeIn(request.getRoleCodes());
            for (int i = 0; i < roles.size(); i++) {
                UserRole ur = UserRole.builder()
                        .user(savedUser)
                        .role(roles.get(i))
                        .isPrimary(i == 0)
                        .assignedAt(java.time.LocalDateTime.now())
                        .build();
                savedUser.getUserRoles().add(ur);
            }
            savedUser = userRepository.save(savedUser);
        }

        UserResponse response = userMapper.toDto(savedUser);
        auditTrailRecorder.record(AuditActionType.USER_CREATE, savedUser.getId(), null, response);
        return response;
    }

    public PageResponse<UserResponse> getUsers(UserSearchRequest request) {
        String keyword = normalizeKeyword(request.getKeyword());
        Pageable pageable = buildPageable(request);

        QUser qUser = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(keyword)) {
            builder.and(qUser.fullName.containsIgnoreCase(keyword)
                    .or(qUser.displayName.containsIgnoreCase(keyword))
                    .or(qUser.email.containsIgnoreCase(keyword))
                    .or(qUser.phone.contains(keyword)));
        }

        if (request.getStatus() != null) {
            builder.and(qUser.status.eq(request.getStatus()));
        }

        if (StringUtils.hasText(request.getRoleCode())) {
            builder.and(qUser.userRoles.any().role.code.eq(request.getRoleCode()));
        }

        if (request.getMemberLevel() != null) {
            builder.and(qUser.memberLevel.eq(request.getMemberLevel()));
        }

        Page<User> userPage = userRepository.findAll(builder, pageable);

        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream().map(userMapper::toDto).toList())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    public UserResponse getUserById(UUID id) {
        return userMapper.toDto(findUserById(id));
    }

    public UserResponse updateUser(UUID id, AdminUpdateUserRequest request) {
        User user = findUserById(id);
        UserResponse oldState = userMapper.toDto(user);
        String email = DataNormalizer.normalizeEmail(request.getEmail());
        String phone = DataNormalizer.normalizePhone(request.getPhone());
        validateRequiredContact(email, phone);
        validateUniqueContacts(email, phone, user.getId());

        String encodedPassword = StringUtils.hasText(request.getPasswordHash())
                ? passwordEncoder.encode(request.getPasswordHash())
                : null;

        userMapper.applyAdminUpdate(user, request, email, phone, encodedPassword);

        if (request.getRoleCodes() != null) {
            user.getUserRoles().clear();
            java.util.List<com.wedservice.backend.module.users.entity.Role> roles = roleRepository.findAllByCodeIn(request.getRoleCodes());
            for (int i = 0; i < roles.size(); i++) {
                user.getUserRoles().add(UserRole.builder()
                        .user(user)
                        .role(roles.get(i))
                        .isPrimary(i == 0)
                        .build());
            }
        }

        User updatedUser = userRepository.save(user);
        UserResponse response = userMapper.toDto(updatedUser);
        auditTrailRecorder.record(AuditActionType.USER_UPDATE, updatedUser.getId(), oldState, response);
        return response;
    }

    public UserResponse deactivateUser(UUID id) {
        User user = findUserById(id);
        UserResponse oldState = userMapper.toDto(user);
        user.setStatus(Status.SUSPENDED);
        user.setDeletedAt(java.time.LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        UserResponse response = userMapper.toDto(updatedUser);
        auditTrailRecorder.record(AuditActionType.USER_DEACTIVATE, updatedUser.getId(), oldState, response);
        return response;
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateRequiredContact(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BadRequestException("At least email or phone must be provided");
        }
    }

    private void validateUniqueContacts(String email, String phone, UUID currentUserId) {
        if (StringUtils.hasText(email)) {
            boolean emailExists = currentUserId == null
                    ? userRepository.existsByEmailIgnoreCase(email)
                    : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);
            if (emailExists) {
                throw new BadRequestException("Email already exists");
            }
        }

        if (StringUtils.hasText(phone)) {
            boolean phoneExists = currentUserId == null
                    ? userRepository.existsByPhone(phone)
                    : userRepository.existsByPhoneAndIdNot(phone, currentUserId);
            if (phoneExists) {
                throw new BadRequestException("Phone already exists");
            }
        }
    }

    private Pageable buildPageable(UserSearchRequest request) {
        String sortBy = request.getSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of " + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction direction = Sort.Direction.fromString(request.getSortDir());
        return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortBy));
    }

    private String normalizeKeyword(String keyword) {
        return DataNormalizer.normalize(keyword);
    }
}
