package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
}
