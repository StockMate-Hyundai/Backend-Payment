package com.stockmate.payment.api.payment.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CategorySumDto {
    private String categoryName;
    private Long totalAmount;
}