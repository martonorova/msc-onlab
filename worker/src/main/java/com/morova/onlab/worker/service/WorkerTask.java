package com.morova.onlab.worker.service;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.JMSProducer;

public class WorkerTask implements Runnable{

    private final JobSubmitRequestDTO job;
    private final JMSProducer jmsProducer;

    public WorkerTask(JobSubmitRequestDTO job, JMSProducer jmsProducer) {
        this.job = job;
        this.jmsProducer = jmsProducer;
    }

    @Override
    public void run() {

        long result = fibonacci(job.getInput());
        job.setResult(result);

        // send result to ActiveMQ broker
        jmsProducer.sendJob(job);
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
