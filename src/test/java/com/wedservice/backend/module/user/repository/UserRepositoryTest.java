package com.wedservice.backend.module.user.repository;

import com.wedservice.backend.module.user.entity.MemberLevel;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import com.wedservice.backend.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

        Page<User> resultByName = userRepository.searchUsers("nguyen", Status.ACTIVE, Role.CUSTOMER, null, PageRequest.of(0, 10));
        Page<User> resultByEmail = userRepository.searchUsers("keyword", Status.ACTIVE, Role.ADMIN, null, PageRequest.of(0, 10));

        assertThat(resultByName.getContent()).hasSize(1);
        assertThat(resultByName.getContent().getFirst().getFullName()).isEqualTo("Nguyen Van A");
        assertThat(resultByEmail.getContent()).hasSize(1);
        assertThat(resultByEmail.getContent().getFirst().getEmail()).isEqualTo("special.keyword@example.com");
    }

    @Test
    void searchUsers_filtersByStatus() {
        userRepository.save(buildUser("Active User", "active@example.com", "0987654321", Status.ACTIVE, Role.CUSTOMER));
        userRepository.save(buildUser("Suspended User", "suspended@example.com", "0987654322", Status.SUSPENDED, Role.CUSTOMER));

        Page<User> activeOnly = userRepository.searchUsers(null, Status.ACTIVE, Role.CUSTOMER, MemberLevel.BRONZE, PageRequest.of(0, 10));
        Page<User> suspendedOnly = userRepository.searchUsers(null, Status.SUSPENDED, Role.CUSTOMER, MemberLevel.BRONZE, PageRequest.of(0, 10));

        assertThat(activeOnly.getContent()).extracting(User::getEmail).containsExactly("active@example.com");
        assertThat(suspendedOnly.getContent()).extracting(User::getEmail).containsExactly("suspended@example.com");
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
