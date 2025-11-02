package com.stockmate.payment.api.payment.repository;

import com.stockmate.payment.api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrderNumber(String orderNumber);

    // 월별 지출 금액
    @Query("""
        SELECT SUM(p.totalAmount)
        FROM Payment p
        WHERE p.userId = :userId
          AND p.status = 'COMPLETED'
          AND FUNCTION('YEAR', p.createdAt) = :year
          AND FUNCTION('MONTH', p.createdAt) = :month
    """)
    Long findMonthlySpending(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

}
