package com.morova.onlab.worker.messaging.activemq;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;
import com.morova.onlab.worker.service.WorkerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.CountDownLatch;

@Component
@ConditionalOnExpression("'${messaging}'.equals('activemq')")
public class JMSListener implements MessageListener {

    @Autowired
    WorkerService workerService;

    @Autowired
    Producer producer;

    Logger logger = LoggerFactory.getLogger(JMSListener.class);

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
            //send heartbeat, so backend is aware, that it must monitor the Job
            producer.sendHeartBeat(job.getId());

            logger.info("Received Message from Queue: " + job.toString());

            CountDownLatch countDownLatch = new CountDownLatch(1);
            workerService.submitJob(job, countDownLatch);

            countDownLatch.await();

        } catch(JMSException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
