package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.ValidateDto;
import com.stockmate.payment.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final WebClient webClient;
//    private final

    @Value("${order.server.url}")
    private String orderServerUrl;

    public ValidateDto getOrderByOrderId(Long orderId) {
        try {
            ApiResponse<ValidateDto> wrapper = webClient.get()
                    .uri(orderServerUrl + "/api/v1/order/validate/{orderId}", orderId)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponse<ValidateDto>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            ValidateDto response = wrapper != null ? wrapper.getData() : null;
            if (response == null) {
                log.info("주문 검증 정보 조회 실패 - orderId: {}, response: {}", orderId, response);
            }
            log.info("✅ 주문 검증 정보 조회 성공 - orderId: {}, response: {}", orderId, response);

            return response;

        } catch (Exception e) {
            log.error("❌ 주문 검증 정보 조회 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new IllegalStateException("Order 서버 호출 실패", e);
        }
    }
}
