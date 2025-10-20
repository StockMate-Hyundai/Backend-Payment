package com.stockmate.payment.common.config.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER"), ADMIN("ROLE_ADMIN"), SUPER_ADMIN("ROLE_SUPER_ADMIN");

    private final String key;
}