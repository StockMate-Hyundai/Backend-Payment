package com.stockmate.payment.common.producer;

import com.stockmate.payment.api.payment.dto.CancelResponseEvent;
import com.stockmate.payment.api.payment.dto.PayResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.pay-success}")
    private String paySuccessTopic;

    @Value("${kafka.topics.pay-failed}")
    private String payFailedTopic;

    @Value("${kafka.topics.cancel-success}")
    private String cancelSuccessTopic;

    @Value("${kafka.topics.cancel-failed}")
    private String cancelFailedTopic;

    public void sendPaySuccess(PayResponseEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                paySuccessTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 성공 이벤트 발송 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getOrderId());
            } else {
                log.error("결제 성공 요청 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendPayFailed(PayResponseEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                payFailedTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 실패 이벤트 발송 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getOrderId());
            } else {
                log.error("결제 실패 요청 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendCancelSuccess(CancelResponseEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                cancelSuccessTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 취소 성공 이벤트 발송 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getOrderId());
            } else {
                log.error("결제 취소 성공 요청 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendCancelFailed(CancelResponseEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                cancelFailedTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 취소 실패 이벤트 발송 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getOrderId());
            } else {
                log.error("결제 취소 실패 요청 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }
}
