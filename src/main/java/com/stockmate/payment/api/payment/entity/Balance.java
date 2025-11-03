package com.stockmate.payment.api.payment.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "balance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance")
    private long balance;

    @Column(name = "user_id")
    private Long userId;
}
