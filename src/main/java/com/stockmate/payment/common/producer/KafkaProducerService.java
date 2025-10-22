package com.stockmate.payment.common.producer;

import com.stockmate.payment.api.payment.dto.DepositDeductionSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.deposit-deduction-success}")
    private String depositDeductionSuccessTopic;

    @Value("${kafka.topics.deposit-deduction-failed}")
    private String depositDeductionFailedTopic;

    public void sendDepositDeductionSuccess(DepositDeductionSuccessEvent event) {
        
    }

}
