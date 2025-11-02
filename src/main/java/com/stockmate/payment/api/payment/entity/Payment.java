package com.stockmate.payment.api.payment.entity;

import com.stockmate.payment.api.payment.dto.CancelRequestEvent;
import com.stockmate.payment.api.payment.dto.PayRequestEvent;
import com.stockmate.payment.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "payment_type")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    public static Payment of(PayRequestEvent p, PaymentStatus status) {
        return Payment.builder()
                .orderNumber(p.getOrderNumber())
                .paymentType(p.getPaymentType())
                .totalAmount((long) p.getTotalPrice())
                .status(status)
                .userId(p.getMemberId())
                .orderId(p.getOrderId())
                .build();
    }

    public static Payment of(CancelRequestEvent p, PaymentStatus status) {
        return Payment.builder()
                .orderNumber(p.getOrderNumber())
                .paymentType(p.getPaymentType())
                .totalAmount((long) p.getTotalPrice())
                .status(status)
                .userId(p.getMemberId())
                .orderId(p.getOrderId())
                .build();
    }
}
