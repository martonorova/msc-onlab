package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class WorkerService {

    private final ThreadPoolExecutor executor;

    @Autowired
    Producer producer;

    private Long currentJobId;

    Logger logger = LoggerFactory.getLogger(WorkerService.class);

    public WorkerService() {
        executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    public void submitJob(JobSubmitRequestDTO job, CountDownLatch countDownLatch) {
        currentJobId = job.getId();

        executor.execute(new WorkerTask(job, producer, countDownLatch));
    }

    public int getBusyThreads() {
        return this.executor.getActiveCount();
    }

    @Scheduled(fixedDelay = 5_000)
    public void sendHeartbeat() {
        if (currentJobId != null) {
            producer.sendHeartBeat(currentJobId);
        }

    }
}
