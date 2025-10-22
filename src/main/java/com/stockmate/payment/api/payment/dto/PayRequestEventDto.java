package com.stockmate.payment.api.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayRequestEventDto {
    private Long orderId;
    private String orderNumber;
    private String paymentType;
    private int totalPrice;
}
