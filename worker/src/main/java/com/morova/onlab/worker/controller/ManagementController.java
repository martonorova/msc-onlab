package com.morova.onlab.worker.controller;

import com.morova.onlab.worker.dto.JobSubmitRequestDTO;
import com.morova.onlab.worker.messaging.JMSProducer;
import com.morova.onlab.worker.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ManagementController {

    @Autowired
    private WorkerService workerService;

    @Autowired
    JMSProducer jmsProducer;

    @GetMapping
    public String hello() {
        return "Hello";
    }

    // TODO health endpoint

    @PostMapping("/jobs")
    public ResponseEntity<String> submitJob(@RequestBody JobSubmitRequestDTO job) {

        // submit job to worker service
//        workerService.submitJob(job);

//        jmsProducer.sendJob(job);

        return ResponseEntity.ok("Job submitted");
    }
}
