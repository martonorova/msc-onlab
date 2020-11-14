package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class WorkerService {

    private final ThreadPoolExecutor executor;

    public WorkerService() {
        executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    public void submitJob(JobSubmitRequestDTO job) {
        executor.execute(new WorkerTask(job));
    }
}
