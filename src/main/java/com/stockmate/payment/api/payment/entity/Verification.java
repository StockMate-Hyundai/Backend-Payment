package com.stockmate.payment.api.payment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "order_id")
    private Long orderId;

    @OneToOne(mappedBy = "verification", fetch = FetchType.LAZY)
    private Payment payment;
}
