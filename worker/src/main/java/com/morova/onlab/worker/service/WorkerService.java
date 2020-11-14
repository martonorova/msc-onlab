package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.JMSProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class WorkerService {

    private final ThreadPoolExecutor executor;

    @Autowired
    JMSProducer jmsProducer;

    public WorkerService() {
        executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    public void submitJob(JobSubmitRequestDTO job) {
        executor.execute(new WorkerTask(job, jmsProducer));
    }
}
