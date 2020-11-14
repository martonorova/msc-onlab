package com.morova.onlab.backend.messaging;

import com.morova.onlab.backend.model.Job;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class JMSProducer {

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.worker.queue}")
    private String queue;

    public void sendJob(Job job){
        try {
            System.out.println("Attempting Send message to Queue: " + queue + "job: " + job.toString());
            JSONObject jsonObject = new JSONObject(job);
            // put a dummy result to send valid JSON
            jsonObject.put("result", -1L);
            jmsTemplate.convertAndSend(queue, jsonObject.toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
