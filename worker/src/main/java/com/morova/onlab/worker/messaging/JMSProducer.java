package com.morova.onlab.worker.messaging;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class JMSProducer {

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.queue}")
    private String queue;

    public void sendJob(JobSubmitRequestDTO job){
        try {
            System.out.println("Attempting Send message to Topic: " + queue);
            jmsTemplate.convertAndSend(queue, job);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
