package com.stockmate.payment.api.payment.dto;

import com.stockmate.payment.api.payment.entity.DepositTransaction;
import com.stockmate.payment.api.payment.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class DepositTransactionResponseDto {
    private TransactionType transactionType;
    private LocalDateTime transactionTime;
    private Long totalAmount;
    private Long orderId;
    private Long balance;

    public static DepositTransactionResponseDto of (DepositTransaction dt) {
        return DepositTransactionResponseDto.builder()
                .transactionType(dt.getTransactionType())
                .transactionTime(dt.getPayment().getUpdatedAt())
                .totalAmount(dt.getAmount())
                .orderId(dt.getPayment().getOrder())
                .balance(dt.getBalance().getBalance())
                .build();
    }
}
