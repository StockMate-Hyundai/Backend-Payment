package com.stockmate.payment.api.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    REQUESTED("REQUESTED"), // 결제 요청됨
    FAILED("FAILED"), // 결제 실패
    COMPLETED("COMPLETED"), // 결제 완료
    CANCEL_FAILED("CANCEL_FAILED"), // 결제 취소
    REFUNDED("REFUNDED"); // 환불 완료

    private final String key;
}
