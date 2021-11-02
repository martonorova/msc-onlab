package com.morova.onlab.backend.messaging.activemq;

import com.morova.onlab.backend.messaging.Producer;
import com.morova.onlab.backend.model.Job;
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

    @Value("${activemq.worker.queue}")
    private String queue;

    Logger logger = LoggerFactory.getLogger(JMSProducer.class);

    @Override
    public void sendJob(Job job){
        try {
            logger.info("Attempting Send message to Queue: " + queue + "job: " + job.toString());
            JSONObject jsonObject = new JSONObject(job);
            // put a dummy result to send valid JSON
            jsonObject.put("result", -1L);
            jmsTemplate.convertAndSend(queue, jsonObject.toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
