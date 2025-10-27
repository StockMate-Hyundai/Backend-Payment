package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
}
