package com.stockmate.payment.api.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    ORDER_COMPLETED("ORDER_COMPLETED"), // 주문 완료
    PENDING_APPROVAL("PENDING_APPROVAL"), // 승인 대기 (재고 차감 및 결제 진행 중)
    PENDING_SHIPPING("PENDING_SHIPPING"), // 출고 대기
    SHIPPING("SHIPPING"), // 배송중
    REJECTED("REJECTED"), // 출고 반려
    DELIVERED("DELIVERED"), // 배송 완료
    RECEIVED("RECEIVED"), // 입고 완료
    CANCELLED("CANCELLED"); // 주문 취소

    private final String key;
}
