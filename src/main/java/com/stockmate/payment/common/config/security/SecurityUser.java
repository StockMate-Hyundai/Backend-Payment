package com.stockmate.payment.common.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityUser {

    private Long id;
    private Long memberId;
    private String email;
    private Role role;

}
