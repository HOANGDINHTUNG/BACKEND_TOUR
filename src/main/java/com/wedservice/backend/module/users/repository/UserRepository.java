package com.wedservice.backend.module.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedservice.backend.module.users.entity.User;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByPhoneAndIdNot(String phone, UUID id);

    @Query("SELECT u FROM User u WHERE u.email = :login OR u.phone = :login")
    Optional<User> findByLoginIdentifier(@Param("login") String login);
}
