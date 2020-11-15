package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.JMSProducer;

import java.util.concurrent.CountDownLatch;

public class WorkerTask implements Runnable{

    private final JobSubmitRequestDTO job;
    private final JMSProducer jmsProducer;
    private final CountDownLatch countDownLatch;

    public WorkerTask(JobSubmitRequestDTO job, JMSProducer jmsProducer, CountDownLatch countDownLatch) {
        this.job = job;
        this.jmsProducer = jmsProducer;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        long result = fibonacci(job.getInput());
        job.setResult(result);

        // send result to ActiveMQ broker
        jmsProducer.sendJob(job);

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
