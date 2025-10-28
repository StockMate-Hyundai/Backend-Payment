package com.stockmate.payment.api.payment.controller;

import com.stockmate.payment.api.payment.entity.Balance;
import com.stockmate.payment.api.payment.service.PaymentService;
import com.stockmate.payment.common.config.security.SecurityUser;
import com.stockmate.payment.common.response.ApiResponse;
import com.stockmate.payment.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 관련 API입니다.")
@RestController
@RequestMapping("api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class paymentController {
    private final PaymentService paymentService;

    @GetMapping("/amount")
    public ResponseEntity<ApiResponse<Balance>> getDepositAmount(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Long userId = securityUser.getMemberId();
        Balance response = paymentService.getDeposit(userId);
        return ApiResponse.success(SuccessStatus.DEPOSIT_CHECK_SUCCESS, response);
    }

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<Void>> depositCharge(
            @RequestParam Long amount,
            @AuthenticationPrincipal SecurityUser securityUser
            ) {
        Long userId = securityUser.getMemberId();
        paymentService.depositCharge(userId, amount);
        return ApiResponse.success_only(SuccessStatus.DEPOSIT_CHARGE_SUCCESS);
    }
}
