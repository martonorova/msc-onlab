package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;

public class WorkerTask implements Runnable{

    private final JobSubmitRequestDTO job;

    public WorkerTask(JobSubmitRequestDTO job) {
        this.job = job;
    }


    @Override
    public void run() {

        long result = fibonacci(job.getInput());

        // send result to ActiveMQ broker

    }

    private long fibonacci(long n) {
        long result = 0L;
        if (n <= 2) {
            return n - 1;
        }
        result = fibonacci(n - 1) + fibonacci(n - 2);
        return result;

    }
}
