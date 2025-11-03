package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.DepositTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {
    @EntityGraph(attributePaths = {"payment"})
    Page<DepositTransaction> findAllByUserId(
            Long userId,
            Pageable pageable
    );
}
