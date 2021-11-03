package com.morova.onlab.worker.messaging.activemq;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${messaging}'.equals('activemq')")
public class JMSProducer implements Producer {

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.backend.queue}")
    private String jobBackendQueue;

    @Value("${activemq.jobstatus.queue}")
    private String heartbeatQueue;

    Logger logger = LoggerFactory.getLogger(JMSProducer.class);

    @Override
    public void sendJob(JobSubmitRequestDTO job){
        try {
            logger.info("Attempting Send message to Queue: " + jobBackendQueue + "job: " + job.toString());
            JSONObject jsonObject = new JSONObject(job);
            jmsTemplate.convertAndSend(jobBackendQueue, jsonObject.toString());
        } catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public void sendHeartBeat(Long jobId) {
        try {
            logger.info("[HEARTBEAT] Send heartbeat for Job: " + jobId.toString());
            jmsTemplate.convertAndSend(heartbeatQueue, jobId.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
