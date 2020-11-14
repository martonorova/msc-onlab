package com.morova.onlab.worker.messaging;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.service.WorkerService;
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
    WorkerService workerService;

    @Override
    @JmsListener(destination = "${activemq.worker.queue}")
    public void onMessage(Message message) {
        try{
            String jsonString = ((ActiveMQTextMessage) message).getText();

            JSONObject jsonObject = new JSONObject(jsonString);
            JobSubmitRequestDTO job = new JobSubmitRequestDTO(
                    jsonObject.getLong("id"),
                    jsonObject.getInt("input"),
                    jsonObject.getLong("result")
            );
            //do additional processing
            System.out.println("Received Message from Queue: " + job.toString());

            workerService.submitJob(job);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
