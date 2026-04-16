package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.CancellationPolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CancellationPolicyRuleRepository extends JpaRepository<CancellationPolicyRule, Long> {

    List<CancellationPolicyRule> findByPolicyIdOrderByMinHoursBeforeDesc(Long policyId);

    boolean existsByPolicyId(Long policyId);
}
