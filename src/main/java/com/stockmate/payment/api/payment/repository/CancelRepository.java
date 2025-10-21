package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.Cancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelRepository extends JpaRepository<Cancel, Long> {
}
