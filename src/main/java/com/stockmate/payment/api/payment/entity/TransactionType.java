package com.stockmate.payment.api.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    CHARGE("CHARGE"),       // 예치금 충전
    USE("USE"),            // 예치금 사용 (결제)
    PAY("PAY"),            // 결제
    REFUND("REFUND"),       // 결제 환불
    ADJUSTMENT("ADJUSTMENT"), // 관리자 수동 조정 (+/- 둘 다 가능)
    WITHDRAW("WITHDRAW");  // 예치금 인출

    private final String key;
}
