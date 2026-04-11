package com.wedservice.backend.module.payments.repository;

import com.wedservice.backend.module.payments.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

}
