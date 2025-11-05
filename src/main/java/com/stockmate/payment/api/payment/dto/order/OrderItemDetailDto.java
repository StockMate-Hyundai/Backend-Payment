package com.stockmate.payment.api.payment.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetailDto {
    private Long partId;
    private int amount;
    private PartDetailResponseDto partDetail;
}