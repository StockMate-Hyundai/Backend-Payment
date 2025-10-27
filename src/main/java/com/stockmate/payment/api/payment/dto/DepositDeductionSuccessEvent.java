package com.stockmate.payment.api.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositDeductionSuccessEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId; // Saga 시도 식별자
}
