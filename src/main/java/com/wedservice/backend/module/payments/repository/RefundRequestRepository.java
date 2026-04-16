package com.wedservice.backend.module.payments.repository;

import com.wedservice.backend.module.payments.entity.RefundRequest;
import com.wedservice.backend.module.payments.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    boolean existsByBookingIdAndStatusIn(Long bookingId, Collection<RefundStatus> statuses);

}
