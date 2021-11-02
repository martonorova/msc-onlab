package com.morova.onlab.worker.messaging.kafka;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${messaging}'.equals('kafka')")
public class JobProducer implements Producer {

    // sends back finished jobs to the backend
    @Value(value = "${kafka.topics.backend}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    Logger logger = LoggerFactory.getLogger(JobProducer.class);

    @Override
    public void sendJob(JobSubmitRequestDTO job) {
        logger.info("Attempting Send message to Topic: " + topicName + "job: " + job.toString());
        JSONObject jsonObject = new JSONObject(job);
        kafkaTemplate.send(topicName, jsonObject.toString());
    }
}
