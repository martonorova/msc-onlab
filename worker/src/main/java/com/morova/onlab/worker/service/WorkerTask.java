package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.Producer;

import java.util.concurrent.CountDownLatch;

public class WorkerTask implements Runnable{

    private final JobSubmitRequestDTO job;
    private final Producer producer;
    private final CountDownLatch countDownLatch;

    public WorkerTask(JobSubmitRequestDTO job, Producer producer, CountDownLatch countDownLatch) {
        this.job = job;
        this.producer = producer;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        long result = fibonacci(job.getInput());
        job.setResult(result);

        // send result to ActiveMQ broker
        producer.sendJob(job);

        countDownLatch.countDown();
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
