package com.morova.onlab.backend.messaging.kafka;

import com.morova.onlab.backend.messaging.Producer;
import com.morova.onlab.backend.model.Job;
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

    // submits jobs to workers to execute
    @Value(value = "${kafka.topics.worker}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    Logger logger = LoggerFactory.getLogger(JobProducer.class);

    @Override
    public void sendJob(Job job) {

        logger.info("Attempting send job to Topic: " + topicName + "job: " + job.toString());

        JSONObject jsonObject = new JSONObject(job);
        // put a dummy result to send valid JSON
        jsonObject.put("result", -1L);

        kafkaTemplate.send(topicName, jsonObject.toString());
    }
}
