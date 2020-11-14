package com.morova.onlab.worker.messaging;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class JMSListener implements MessageListener {

    @Autowired
    WorkerService workerService;

    @Override
    @JmsListener(destination = "${activemq.worker.queue}")
    public void onMessage(Message message) {
        try{
            ObjectMessage objectMessage = (ObjectMessage)message;
            JobSubmitRequestDTO job = (JobSubmitRequestDTO) objectMessage.getObject();
            //do additional processing
            System.out.println("Received Message from Queue: " + job.toString());

            workerService.submitJob(job);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
