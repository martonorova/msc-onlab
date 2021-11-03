package com.morova.onlab.backend.scheduling;

import com.morova.onlab.backend.messaging.Producer;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@ConditionalOnProperty(name = "heartbeats.enabled", matchIfMissing = false)
public class RetryService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Producer producer;

    private static final Logger logger = LoggerFactory.getLogger(RetryService.class);

    // holds Job ids with the timestamp of the last received heartbeat message
    private final Map<Long, Long> jobHeartBeats = new HashMap<>();

    @Scheduled(fixedDelay = 10_000)
    public void resubmitLostJobs() {
        logger.info("[CHECK RETRY] Started");
        List<Job> unfinishedJobs = jobRepository.findUnfinishedJobs();

        unfinishedJobs.forEach(job -> {
            if (jobHeartBeats.containsKey(job.getId())) {
                Long lastHeartBeatTimestamp = jobHeartBeats.get(job.getId());

                // if last heartbeat older than 1 minute --> assume job is lost, have to resubmit
                if (lastHeartBeatTimestamp + 60 * 1000 < System.currentTimeMillis()) {
                    producer.sendJob(job);
                    logger.info("[RESUBMIT] Job resubmitted: " + job.toString());

                    // remove Job id from map to avoid multiple resubmitting
                    // when resubmitting, the Job enters the queue and it can take time for a worker to pick it up
                    // so in the meantime, as no heartbeat is received, to Job gets resubmitted again
                    jobHeartBeats.remove(job.getId());
                }

            } else {
                logger.info("[NO HEARTBEAT INFO] No heartbeat info for Job: " + job.toString());
            }
        });

        logger.info("[CHECK RETRY] Finished");
    }

    @Scheduled(fixedDelay = 120_000)
    public void cleanUpOldHeartBeats() {
        logger.info("[CLEAN UP HEARTBEATS] Started");
        List<Job> finishedJobs = jobRepository.findFinishedJobs();

        finishedJobs.forEach(job -> {
            jobHeartBeats.remove(job.getId());
        });

        logger.info("[CLEAN UP HEARTBEATS] Finished");
    }

    @ConditionalOnExpression("'${messaging}'.equals('activemq')")
    @JmsListener(destination = "${activemq.jobstatus.queue}")
    public void onJMSMessage(Message message) {
        try{
            String messageText = ((ActiveMQTextMessage) message).getText();

            logger.info("[JOBSTATUS] Received heartbeat Job id: " + messageText);

            // update Job timestamp in map
            Long jobId = Long.parseLong(messageText);
            jobHeartBeats.put(jobId, System.currentTimeMillis());

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
