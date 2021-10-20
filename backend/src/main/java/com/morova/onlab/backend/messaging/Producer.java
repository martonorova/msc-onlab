package com.morova.onlab.backend.messaging;

import com.morova.onlab.backend.model.Job;

public interface Producer {
    void sendJob(Job job);
}
