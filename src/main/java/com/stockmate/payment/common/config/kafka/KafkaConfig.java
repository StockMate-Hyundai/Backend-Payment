//package com.stockmate.payment.common.config.kafka;
//
//import com.stockmate.user.api.user.dto.UserRegistrationFailedEvent;
//import com.stockmate.user.api.user.dto.UserRoleChangeEvent;
//import com.stockmate.user.api.user.dto.UserStatusChangeEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.config.TopicBuilder;
//import org.springframework.kafka.core.*;
//import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
//import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
//import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@Slf4j
//public class KafkaConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Value("${kafka.topics.user-registration}")
//    private String userRegistrationTopic;
//
//    @Value("${kafka.topics.user-registration-failed}")
//    private String userRegistrationFailedTopic;
//
//    @Bean
//    public KafkaAdmin kafkaAdmin() {
//        Map<String, Object> configs = new HashMap<>();
//        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        log.info("Kafka Admin 설정 완료 - Bootstrap Servers: {}", bootstrapServers);
//        return new KafkaAdmin(configs);
//    }
//
//    @Bean
//    public NewTopic userRegistrationTopic() {
//        log.info("Kafka 토픽 생성: {}", userRegistrationTopic);
//        return TopicBuilder.name(userRegistrationTopic).partitions(3).replicas(1).build();
//    }
//
//    @Bean
//    public NewTopic userRegistrationFailedTopic() {
//        log.info("Kafka 토픽 생성: {}", userRegistrationFailedTopic);
//        return TopicBuilder.name(userRegistrationFailedTopic).partitions(3).replicas(1).build();
//    }
//
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
//        classIdMapping.put("userStatusChange", UserStatusChangeEvent.class);
//        classIdMapping.put("userRoleChange", UserRoleChangeEvent.class);
//        classIdMapping.put("userRegistrationFailed", UserRegistrationFailedEvent.class);
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
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.stockmate.auth.api.member.dto,com.stockmate.user.api.user.dto");
//        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.stockmate.user.api.user.dto.UserRegistrationEvent");
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
//}
//
