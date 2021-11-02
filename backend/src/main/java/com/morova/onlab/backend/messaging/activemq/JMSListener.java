package com.morova.onlab.backend.messaging.activemq;

import com.morova.onlab.backend.controller.HealthController;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.time.LocalDateTime;

@Component
@ConditionalOnExpression("'${messaging}'.equals('activemq')")
public class JMSListener implements MessageListener {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    HealthController healthController;

    Logger logger = LoggerFactory.getLogger(JMSListener.class);

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
            logger.info("Received Message from Queue: " + job.toString());

            // negative ID means a test job
            if (job.getId() > 0) {
                jobRepository.save(job);
            }


            // update health
            healthController.setLastSuccessfulExecutionTime(LocalDateTime.now());

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
