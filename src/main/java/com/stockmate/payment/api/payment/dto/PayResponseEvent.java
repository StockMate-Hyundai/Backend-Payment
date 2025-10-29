package com.stockmate.payment.api.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayResponseEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId; // Saga 시도 식별자
    private Boolean isSuccess;
    private String etc;

    public static PayResponseEvent of(PayRequestEvent event, Boolean isSuccess, String etc) {
        return PayResponseEvent.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .approvalAttemptId("PAY-" + System.currentTimeMillis())
                .isSuccess(isSuccess)
                .etc(etc)
                .build();
    }
}
