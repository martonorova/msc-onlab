package com.morova.onlab.backend.messaging.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnExpression("'${messaging}'.equals('kafka')")
public class KafkaProducerConfig {

    @Value(value = "${kafka.bootstrap-servers}")
    private String bootstrapAddress;

    public Map<String, Object> jobProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    public ProducerFactory<String, String> jobProducerFactory() {
        return new DefaultKafkaProducerFactory<>(jobProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> jobKafkaTemplate() {
        return new KafkaTemplate<>(jobProducerFactory());
    }

}
