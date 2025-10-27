package com.stockmate.payment.common.consumer;

import com.stockmate.payment.api.payment.dto.CancelRequestEvent;
import com.stockmate.payment.api.payment.entity.PaymentType;
import com.stockmate.payment.api.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelRequestConsumer {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${kafka.topics.cancel-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )

    public void handleCancelRequest(
            @Payload CancelRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("결제 취소 요청 수신 - Topic: {}, Partition: {}, Offset: {}, OrderId: {}, PaymentType: {}",
                topic, partition, offset, event.getOrderId(), event.getPaymentType());

        try {
            if (event.getPaymentType() == PaymentType.DEPOSIT) { // 예치금 결제
                paymentService.handleDepositCancelRequest(event);
//            } else if (event.getPaymentType() == PaymentType.CARD) { // 카드 결제
//                paymentService.handleCardPayRequest(event);
            } else {
                log.error("지원하지 않는 결제 타입: {}", event.getPaymentType());
                throw new IllegalArgumentException("지원하지 않는 결제 타입: " + event.getPaymentType());
            }
            log.info("결제 취소 요청 처리 완료 - OrderId: {}", event.getOrderId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("결제 취소 요청 처리 실패 - OrderId: {}, Error: {}", event.getOrderId(), e.getMessage(), e);
            // DLQ(Dead Letter Queue)로 전송하거나 재시도 정책에 따라 처리
            // 현재는 acknowledge하지 않아 재처리되도록 함
            throw e;
        }
    }
}
