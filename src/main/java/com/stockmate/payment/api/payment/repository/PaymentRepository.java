package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
