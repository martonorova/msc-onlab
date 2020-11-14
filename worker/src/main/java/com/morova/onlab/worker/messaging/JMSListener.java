package com.morova.onlab.worker.messaging;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class JMSListener implements MessageListener {

    @Override
    @JmsListener(destination = "${activemq.queue}")
    public void onMessage(Message message) {
        try{
            ObjectMessage objectMessage = (ObjectMessage)message;
            JobSubmitRequestDTO job = (JobSubmitRequestDTO) objectMessage.getObject();
            //do additional processing
            System.out.println("Received Message from Queue: " + job.getId());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
