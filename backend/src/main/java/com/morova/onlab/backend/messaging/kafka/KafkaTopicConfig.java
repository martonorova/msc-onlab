package com.morova.onlab.backend.messaging.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnExpression("'${messaging}'.equals('kafka')")
public class KafkaTopicConfig {

    @Value(value = "${kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${kafka.topics.worker}")
    private String workerTopic;

    @Value(value = "${kafka.topics.backend}")
    private String backendTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic jobWorkerTopic() {
        return new NewTopic(workerTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic jobBackendTopic() {
        return new NewTopic(backendTopic, 1, (short) 1);
    }

}
