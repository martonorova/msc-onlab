package com.morova.onlab.backend.messaging.kafka;

import com.morova.onlab.backend.controller.HealthController;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnExpression("'${messaging}'.equals('kafka')")
public class JobConsumer {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    HealthController healthController;

    @KafkaListener(topics = "${kafka.topics.backend}", groupId = "${kafka.consumer.group-id}")
    public void consumeJob(String jobJson) {

        JSONObject jsonObject = new JSONObject(jobJson);

        Job job = new Job(
                jsonObject.getLong("id"),
                jsonObject.getInt("input"),
                jsonObject.getLong("result")
        );

        System.out.println("Received job: " + job.toString());

        // negative ID means a test job
        if (job.getId() > 0) {
            jobRepository.save(job);
        }

        // update health
        healthController.setLastSuccessfulExecutionTime(LocalDateTime.now());
    }
}
