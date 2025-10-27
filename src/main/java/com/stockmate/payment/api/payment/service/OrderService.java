package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.ValidateDto;
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
        ValidateDto response;
        try {
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/order/validate/{orderId}")
                            .build(orderId)
                    )
                    .retrieve()
                    .bodyToMono(ValidateDto.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("✅ 주문 검증 정보 조회 성공 - orderId: {}, response: {}", orderId, response);
            return response;

        } catch (Exception e) {
            log.error("❌ 주문 검증 정보 조회 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new IllegalStateException("Order 서버 호출 실패", e);
        }

    }
}
