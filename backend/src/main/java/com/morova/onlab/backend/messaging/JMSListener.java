package com.morova.onlab.backend.messaging;

import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class JMSListener implements MessageListener {

    @Autowired
    JobRepository jobRepository;

    @Override
    @JmsListener(destination = "${activemq.backend.queue}")
    public void onMessage(Message message) {
        try{
            String jsonString = ((ActiveMQTextMessage) message).getText();
            JSONObject jsonObject = new JSONObject(jsonString);

            Job job = new Job(
                    jsonObject.getLong("id"),
                    jsonObject.getInt("input"),
                    jsonObject.getLong("result")
            );
            //do additional processing
            System.out.println("Received Message from Queue: " + job.toString());

            jobRepository.save(job);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
