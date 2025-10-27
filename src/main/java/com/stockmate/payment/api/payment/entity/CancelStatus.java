package com.stockmate.payment.api.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelStatus {
    REQUESTED("REQUESTED"), // 취소 요청됨
    APPROVED("APPROVED"), // 취소 승인됨
    REJECTED("REJECTED"); // 취소 거절됨

    private final String key;
    }
