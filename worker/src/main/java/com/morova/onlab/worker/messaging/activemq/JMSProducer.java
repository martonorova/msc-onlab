package com.morova.onlab.worker.messaging.activemq;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;
import org.json.JSONObject;
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
    private String queue;

    @Override
    public void sendJob(JobSubmitRequestDTO job){
        try {
            System.out.println("Attempting Send message to Queue: " + queue + "job: " + job.toString());
            JSONObject jsonObject = new JSONObject(job);
            jmsTemplate.convertAndSend(queue, jsonObject.toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
