package com.wedservice.backend.module.user.repository;

import com.wedservice.backend.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByFullNameContainingIgnoreCaseAndActive(String fullName, Boolean active, Pageable pageable);

    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<User> findByActive(Boolean active, Pageable pageable);
}
