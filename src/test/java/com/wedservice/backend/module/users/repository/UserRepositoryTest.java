package com.wedservice.backend.module.users.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;

import com.wedservice.backend.module.users.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmailIgnoreCase_returnsTrue_whenEmailExistsWithDifferentCase() {
        userRepository.save(buildUser("Nguyen Van A", "a@example.com", "0987654321", Status.ACTIVE, Role.CUSTOMER));

        assertThat(userRepository.existsByEmailIgnoreCase("A@EXAMPLE.COM")).isTrue();
    }

    @Test
    void searchUsers_matchesKeywordAgainstFullNameOrEmail() {
        userRepository.save(buildUser("Nguyen Van A", "first@example.com", "0987654321", Status.ACTIVE, Role.CUSTOMER));
        userRepository.save(buildUser("Tran Thi B", "special.keyword@example.com", "0987654322", Status.ACTIVE, Role.ADMIN));

        Page<User> resultByName = searchUsersRepository("nguyen", Status.ACTIVE, Role.CUSTOMER, null, PageRequest.of(0, 10));
        Page<User> resultByEmail = searchUsersRepository("keyword", Status.ACTIVE, Role.ADMIN, null, PageRequest.of(0, 10));

        assertThat(resultByName.getContent()).hasSize(1);
        assertThat(resultByName.getContent().getFirst().getFullName()).isEqualTo("Nguyen Van A");
        assertThat(resultByEmail.getContent()).hasSize(1);
        assertThat(resultByEmail.getContent().getFirst().getEmail()).isEqualTo("special.keyword@example.com");
    }

    @Test
    void searchUsers_filtersByStatus() {
        userRepository.save(buildUser("Active User", "active@example.com", "0987654321", Status.ACTIVE, Role.CUSTOMER));
        userRepository.save(buildUser("Suspended User", "suspended@example.com", "0987654322", Status.SUSPENDED, Role.CUSTOMER));

        Page<User> activeOnly = searchUsersRepository(null, Status.ACTIVE, Role.CUSTOMER, null, PageRequest.of(0, 10));
        Page<User> suspendedOnly = searchUsersRepository(null, Status.SUSPENDED, Role.CUSTOMER, null, PageRequest.of(0, 10));

        assertThat(activeOnly.getContent()).extracting(User::getEmail).containsExactly("active@example.com");
        assertThat(suspendedOnly.getContent()).extracting(User::getEmail).containsExactly("suspended@example.com");
    }

    private Page<User> searchUsersRepository(String keyword, Status status, Role role, MemberLevel memberLevel, PageRequest pageRequest) {
        QUser qUser = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(keyword)) {
            builder.and(qUser.fullName.containsIgnoreCase(keyword)
                    .or(qUser.displayName.containsIgnoreCase(keyword))
                    .or(qUser.email.containsIgnoreCase(keyword))
                    .or(qUser.phone.contains(keyword)));
        }
        if (status != null) builder.and(qUser.status.eq(status));
        if (role != null) builder.and(qUser.role.eq(role));
        if (memberLevel != null) builder.and(qUser.memberLevel.eq(memberLevel));
        
        return userRepository.findAll(builder, pageRequest);
    }

    private User buildUser(String fullName, String email, String phone, Status status, Role role) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash("encoded-password")
                .phone(phone)
                .status(status)
                .role(role)
                .build();
        return user;
    }
}
