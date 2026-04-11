package com.wedservice.backend.module.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByPhoneAndIdNot(String phone, UUID id);

    @Query("""
            select u from User u
            where lower(u.email) = lower(:login)
               or u.phone = :login
            """)
    Optional<User> findByLoginIdentifier(@Param("login") String login);

    @Query("""
            select u from User u
            where (:keyword is null
                or lower(u.fullName) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(u.displayName, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(u.email, '')) like lower(concat('%', :keyword, '%'))
                or coalesce(u.phone, '') like concat('%', :keyword, '%'))
              and (:status is null or u.status = :status)
              and (:role is null or u.role = :role)
              and (:memberLevel is null or u.memberLevel = :memberLevel)
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            @Param("role") Role role,
            @Param("memberLevel") MemberLevel memberLevel,
            Pageable pageable
    );
}
