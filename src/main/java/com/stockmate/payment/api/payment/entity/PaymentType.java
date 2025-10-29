package com.stockmate.payment.api.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
    DEPOSIT("DEPOSIT"),
    CARD("CARD");

    private final String key;
}