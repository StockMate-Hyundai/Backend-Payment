package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.order.OrderItemDetailDto;
import com.stockmate.payment.api.payment.dto.order.ValidateDto;
import com.stockmate.payment.api.payment.dto.order.OrderDetailResponseDto;
import com.stockmate.payment.api.payment.dto.order.TransactionPartDetailDto;
import com.stockmate.payment.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public List<TransactionPartDetailDto> getOrderDetail(Long orderId) {
        log.info("[OrderDetail] 📌 상세조회 요청 시작 → orderId={}", orderId);

        try {
            ApiResponse<OrderDetailResponseDto> wrapper = webClient.get()
                    .uri(orderServerUrl + "/api/v1/order/detail?orderId=" + orderId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<OrderDetailResponseDto>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            OrderDetailResponseDto response = wrapper != null ? wrapper.getData() : null;

            if (response == null || response.getOrderItems() == null) {
                log.warn("[OrderDetail] ⚠️ data가 존재하지 않습니다. → orderId={}", orderId);
                return Collections.emptyList();
            }

            log.info("[OrderDetail] 🎯 상세조회 성공 → orderId={}, result={}", orderId, response);

            List<TransactionPartDetailDto> partDetail =
                    response.getOrderItems().stream()
                            .map(OrderItemDetailDto::getPartDetail)
                            .filter(Objects::nonNull)
                            .map(TransactionPartDetailDto::of)
                            .toList();

            return partDetail;

        } catch (Exception e) {
            log.error("[OrderDetail] ❌ 상세조회 실패 → orderId={}, message={}", orderId, e.getMessage(), e);
            throw e;
        }
    }
}
