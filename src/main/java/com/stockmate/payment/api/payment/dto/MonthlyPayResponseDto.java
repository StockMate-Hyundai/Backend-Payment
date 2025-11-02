package com.stockmate.payment.api.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MonthlyPayResponseDto {
    private String month;
    private Long totalAmount;

    public static MonthlyPayResponseDto of(String month, Long totalAmount) {
        return MonthlyPayResponseDto.builder()
                .month(month)
                .totalAmount(totalAmount)
                .build();
    }
}
