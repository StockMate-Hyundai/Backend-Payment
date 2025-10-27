package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.DepositTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {
}
