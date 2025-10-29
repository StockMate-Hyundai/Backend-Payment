package com.stockmate.payment.api.payment.dto;

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
public class ValidateDto {
    private Long orderId;
    private String orderNumber;
    private String paymentType;
    private int totalPrice;
    private String orderStatus;
}
