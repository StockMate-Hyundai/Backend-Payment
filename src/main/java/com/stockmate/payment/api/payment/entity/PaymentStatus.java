package com.stockmate.payment.api.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    ORDERED("ORDERED"), // 주문됨
    APPROVAL_PENDING("APPROVAL_PENDING"), // 결제승인대기
    APPROVED("APPROVED"), // 결제 완료
    CANCELLED("CANCELLED"), // 결제 취소
    REFUNDED("REFUNDED"); // 환불 완료

    private final String key;
}
