package com.stockmate.payment.api.payment.dto.payment;

import com.stockmate.payment.api.payment.dto.order.DepositPartDetailDTO;
import com.stockmate.payment.api.payment.entity.DepositTransaction;
import com.stockmate.payment.api.payment.entity.Payment;
import com.stockmate.payment.api.payment.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DepositTransactionResponseDto {
    private Long transactionId;
    private TransactionType transactionType;
    private LocalDateTime transactionTime;
    private Long totalAmount;
    private Long orderId;
    private List<DepositPartDetailDTO> orderItems;
    private Long balance;

    public static DepositTransactionResponseDto of (DepositTransaction dt, List<DepositPartDetailDTO> pd) {
        Payment payment = dt.getPayment();
        return DepositTransactionResponseDto.builder()
                .transactionId(dt.getId())
                .transactionType(dt.getTransactionType())
                .transactionTime(dt.getUpdatedAt())
                .totalAmount(dt.getAmount())
                .orderId(payment != null ? payment.getOrderId() : null)
                .orderItems(pd)
                .balance(dt.getCurrentBalance())
                .build();
    }
}
