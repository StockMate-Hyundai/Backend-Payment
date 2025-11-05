package com.stockmate.payment.api.payment.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DepositPartDetailDTO {
    private Long id;
    private String name;
    private String image;
    private String korName;
    private String categoryName;
}