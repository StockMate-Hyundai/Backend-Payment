package com.stockmate.payment.api.payment.dto.payment;

import com.stockmate.payment.api.payment.dto.order.PartDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPartDetailDto { // 예치금 내역 부품 정보
    private Long id;
    private String name;
    private String image;
    private int category;
    private String korName;
    private String categoryName;

    public static TransactionPartDetailDto of (PartDetailResponseDto pd) {
        return TransactionPartDetailDto.builder()
                .id(pd.getId())
                .name(pd.getName())
                .image(pd.getImage())
                .category(pd.getCategory())
                .korName(pd.getKorName())
                .categoryName(pd.getCategoryName())
                .build();
    }
}
