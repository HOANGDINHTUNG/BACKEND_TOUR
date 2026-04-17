package com.wedservice.backend.module.promotions.repository;

import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherUserClaimRepository extends JpaRepository<VoucherUserClaim, Long> {
    Optional<VoucherUserClaim> findByVoucherIdAndUserId(Long voucherId, UUID userId);

    List<VoucherUserClaim> findByUserIdOrderByClaimedAtDescIdDesc(UUID userId);
}
