package com.stockmate.payment.api.payment.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayCancelResponseEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId; // Saga 시도 식별자
    private Boolean isSuccess;
    private String message;

    public static PayCancelResponseEvent of (PayCancelRequestEvent c, Boolean isSuccess, String message) {
        return PayCancelResponseEvent.builder()
                .orderId(c.getOrderId())
                .orderNumber(c.getOrderNumber())
                .approvalAttemptId("CANCEL-" + System.currentTimeMillis())
                .isSuccess(isSuccess)
                .message(message)
                .build();
    }
}
