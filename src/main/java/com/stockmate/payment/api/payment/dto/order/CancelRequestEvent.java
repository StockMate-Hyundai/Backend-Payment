package com.stockmate.payment.api.payment.dto.order;

import com.stockmate.payment.api.payment.entity.OrderStatus;
import com.stockmate.payment.api.payment.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelRequestEvent {
    private Long orderId;
    private Long memberId;
    private String orderNumber;
    private PaymentType paymentType;
    private int totalPrice;
    private OrderStatus orderStatus;
}
