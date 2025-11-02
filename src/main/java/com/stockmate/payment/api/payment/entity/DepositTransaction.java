package com.stockmate.payment.api.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deposit_transaction")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "amount")
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_id")
    private Balance balance;

    private Long currentBalance; // 거래 시점의 잔액

    private Long userId;


    // 결제
    public static DepositTransaction of (Payment p, Balance b, Long userId) {
        return DepositTransaction.builder()
                .transactionType(TransactionType.PAY)
                .amount((long) p.getTotalAmount())
                .payment(p)
                .balance(b)
                .currentBalance(b.getBalance())
                .userId(userId)
                .build();
    }

    // 충전
    public static DepositTransaction of (Long amount, Balance b, Long userId) {
        return DepositTransaction.builder()
                .transactionType(TransactionType.CHARGE)
                .amount(amount)
                .balance(b)
                .currentBalance(b.getBalance())
                .userId(userId)
                .build();
    }
}
