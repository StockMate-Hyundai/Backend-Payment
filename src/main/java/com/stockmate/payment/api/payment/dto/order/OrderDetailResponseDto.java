package com.stockmate.payment.api.payment.dto.order;

import com.stockmate.payment.api.payment.entity.OrderStatus;
import com.stockmate.payment.api.payment.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponseDto {
    private Long orderId;
    private String orderNumber;
    private Long memberId;
    private UserBatchResponseDto userInfo;
    private List<OrderItemDetailDto> orderItems;
    private PaymentType paymentType;
    private String etc;
    private String rejectedMessage;
    private String carrier;
    private String trackingNumber;
    private LocalDate requestedShippingDate;
    private LocalDate shippingDate;
    private int totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}