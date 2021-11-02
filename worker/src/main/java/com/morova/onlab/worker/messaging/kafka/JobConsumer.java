package com.morova.onlab.worker.messaging.kafka;


import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.service.WorkerService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@ConditionalOnExpression("'${messaging}'.equals('kafka')")
public class JobConsumer {

    @Autowired
    WorkerService workerService;

    Logger logger = LoggerFactory.getLogger(JobConsumer.class);

    @KafkaListener(topics = "${kafka.topics.worker}", groupId = "${kafka.consumer.group-id}")
    public void onMessage(String jobJson) {
        try {
            JSONObject jsonObject = new JSONObject(jobJson);
            JobSubmitRequestDTO job = new JobSubmitRequestDTO(
                    jsonObject.getLong("id"),
                    jsonObject.getInt("input"),
                    jsonObject.getLong("result")
            );
            //do additional processing
            logger.info("Received Message from Queue: " + job.toString());

            CountDownLatch countDownLatch = new CountDownLatch(1);
            workerService.submitJob(job, countDownLatch);

            countDownLatch.await();

        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
