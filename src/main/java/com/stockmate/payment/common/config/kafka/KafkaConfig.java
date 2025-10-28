package com.stockmate.payment.common.config.kafka;

import com.stockmate.payment.api.payment.dto.CancelRequestEvent;
import com.stockmate.payment.api.payment.dto.CancelResponseEvent;
import com.stockmate.payment.api.payment.dto.PayRequestEvent;
import com.stockmate.payment.api.payment.dto.PayResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // producerConfig
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> classIdMapping = new HashMap<>();
        classIdMapping.put("payFailed", PayResponseEvent.class);
        typeMapper.setIdClassMapping(classIdMapping);
        jsonSerializer.setTypeMapper(typeMapper);

        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(Object.class);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> classIdMapping = new HashMap<>();

        classIdMapping.put("payRequest", PayRequestEvent.class);
        classIdMapping.put("cancelRequest", CancelRequestEvent.class);
        typeMapper.setIdClassMapping(classIdMapping);

        jsonDeserializer.setTypeMapper(typeMapper);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
                (record, exception) -> log.error("Kafka 메시지 처리 실패 - 토픽: {}, 파티션: {}, 오프셋: {}, 에러: {}",
                        record.topic(), record.partition(), record.offset(), exception.getMessage()),
                new org.springframework.util.backoff.FixedBackOff(1000L, 3)
        ));
        log.info("Kafka Listener Container Factory 설정 완료");
        return factory;
    }
}

//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//
//        // Producer가 메시지 헤더에 타입 ID를 추가하도록 설정합니다.
//        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>();
//        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
//        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
//
//        // [FIXED] 에러 수정을 위해 Map의 Key와 Value 타입을 변경하고, 데이터를 그에 맞게 수정합니다.
//        Map<String, Class<?>> classIdMapping = new HashMap<>();
//        classIdMapping.put("sendPaySuccess", PayResponseEvent.class);
//        classIdMapping.put("sendPayFailed", PayResponseEvent.class);
//        classIdMapping.put("sendCancelSuccess", CancelResponseEvent.class);
//        classIdMapping.put("sendCancelFailed", CancelResponseEvent.class);
//        typeMapper.setIdClassMapping(classIdMapping);
//        jsonSerializer.setTypeMapper(typeMapper);
//
//        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
//    }
//
//    @Bean
//    public KafkaTemplate<String, Object> kafkaTemplate() {
//        log.info("KafkaTemplate 생성 (Type Mapping 적용)");
//        return new KafkaTemplate<>(producerFactory());
//    }
//
//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
//
//        // ✅ 여기가 중요 — Payment 관련 DTO로 변경해야 함
//        props.put(JsonDeserializer.TRUSTED_PACKAGES,
//                "com.stockmate.payment.api.payment.dto,com.stockmate.order.api.order.dto,com.stockmate.parts.api.parts.dto");
//        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
//                "com.stockmate.payment.api.payment.dto.PayRequestEvent");
//
//        log.info("Kafka Consumer Factory 설정 완료 - Bootstrap Servers: {}", bootstrapServers);
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//
//        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
//                (record, exception) -> {
//                    log.error("Kafka 메시지 처리 실패 - 토픽: {}, 파티션: {}, 오프셋: {}, 에러: {}",
//                            record.topic(), record.partition(), record.offset(), exception.getMessage());
//                },
//                new org.springframework.util.backoff.FixedBackOff(1000L, 3)
//        ));
//
//        log.info("Kafka Listener Container Factory 설정 완료");
//        return factory;
//    }

//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//
//        // Producer가 메시지 헤더에 타입 ID를 추가하도록 설정합니다.
//        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>();
//        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
//        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
//
//        // [FIXED] 에러 수정을 위해 Map의 Key와 Value 타입을 변경하고, 데이터를 그에 맞게 수정합니다.
//        Map<String, Class<?>> classIdMapping = new HashMap<>();
//        classIdMapping.put("sendPaySuccess", PayResponseEvent.class);
//        classIdMapping.put("sendPayFailed", PayResponseEvent.class);
//        classIdMapping.put("sendCancelSuccess", CancelResponseEvent.class);
//        classIdMapping.put("sendCancelFailed", CancelResponseEvent.class);
//        typeMapper.setIdClassMapping(classIdMapping);
//        jsonSerializer.setTypeMapper(typeMapper);
//
//        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
//    }
//
//    @Bean
//    public KafkaTemplate<String, Object> kafkaTemplate() {
//        log.info("KafkaTemplate 생성 (Type Mapping 적용)");
//        return new KafkaTemplate<>(producerFactory());
//    }
//
//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
//
//        // ✅ 여기가 중요 — Payment 관련 DTO로 변경해야 함
//        props.put(JsonDeserializer.TRUSTED_PACKAGES,
//                "com.stockmate.payment.api.payment.dto,com.stockmate.order.api.order.dto,com.stockmate.parts.api.parts.dto");
//        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
//                "com.stockmate.payment.api.payment.dto.PayRequestEvent");
//
//        log.info("Kafka Consumer Factory 설정 완료 - Bootstrap Servers: {}", bootstrapServers);
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//
//        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
//                (record, exception) -> {
//                    log.error("Kafka 메시지 처리 실패 - 토픽: {}, 파티션: {}, 오프셋: {}, 에러: {}",
//                            record.topic(), record.partition(), record.offset(), exception.getMessage());
//                },
//                new org.springframework.util.backoff.FixedBackOff(1000L, 3)
//        ));
//
//        log.info("Kafka Listener Container Factory 설정 완료");
//        return factory;
//    }

