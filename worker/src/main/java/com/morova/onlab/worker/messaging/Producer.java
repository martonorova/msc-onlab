package com.morova.onlab.worker.messaging;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;

public interface Producer {
    void sendJob(JobSubmitRequestDTO job);
}
