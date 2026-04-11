package com.wedservice.backend.module.payments.repository;

import com.wedservice.backend.module.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
