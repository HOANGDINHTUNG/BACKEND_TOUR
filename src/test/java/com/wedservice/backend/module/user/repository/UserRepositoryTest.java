package com.wedservice.backend.module.user.repository;

import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmailIgnoreCase_returnsTrue_whenEmailExistsWithDifferentCase() {
        User user = User.builder()
                .fullName("Nguyen Van A")
                .email("test@example.com")
                .password("encoded")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM")).isTrue();
    }
}
