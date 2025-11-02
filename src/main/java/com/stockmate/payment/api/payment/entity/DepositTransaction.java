package com.stockmate.payment.api.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposit_transaction")
@Getter
@Setter
@AllArgsConstructor
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

    public static DepositTransaction of (Payment p, TransactionType transactionType, Balance b) {
        return DepositTransaction.builder()
                .amount((long) p.getTotalAmount())
                .transactionType(transactionType)
                .payment(p)
                .balance(b)
                .build();
    }

    public static DepositTransaction of (TransactionType transactionType, Balance b) {
        return DepositTransaction.builder()
                .transactionType(transactionType)
                .balance(b)
                .build();
    }
}
