package com.stockmate.payment.common.consumer;

import com.stockmate.payment.api.payment.dto.PayRequestEventDto;
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
public class PayRequestConsumer {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${kafka.topics.pay-request}",

            containerFactory = "kafkaListenerContainerFactory"
    )

    public void handlePayRequest(
            @Payload PayRequestEventDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        paymentService.handlePayRequest(event);
        acknowledgment.acknowledge();
    }

}
