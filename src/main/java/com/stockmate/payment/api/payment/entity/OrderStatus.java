package com.stockmate.payment.api.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    ORDER_COMPLETED("ORDER_COMPLETED"), // 주문 완료
    PAY_COMPLETED("PAY_COMPLETED"),
    PENDING_APPROVAL("PENDING_APPROVAL"), // 승인 대기 (재고 차감 및 결제 진행 중)
    FAILED("FAILED"), // 결제 실패
    PENDING_SHIPPING("PENDING_SHIPPING"), // 출고 대기
    SHIPPING("SHIPPING"), // 배송중
    PENDING_RECEIVING("PENDING_RECEIVING"), // 입고 대기
    REJECTED("REJECTED"), // 출고 반려
    DELIVERED("DELIVERED"), // 배송 완료
    RECEIVED("RECEIVED"), // 입고 완료
    REFUNDED("REFUNDED"), // 환불 완료
    REFUND_REJECTED("REFUND_REJECTED"),
    CANCELLED("CANCELLED"); // 주문 취소

    private final String key;
}
