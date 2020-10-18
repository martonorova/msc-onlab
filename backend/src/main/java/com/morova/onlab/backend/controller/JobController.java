package com.morova.onlab.backend.controller;

import com.morova.onlab.backend.exception.JobNotFoundException;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> result = jobRepository.findAll();
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable(value = "id") long id) {
        try {
            Job result = jobRepository
                    .findById(id)
                    .orElseThrow(() -> new JobNotFoundException(id));
            return ResponseEntity.ok().body(result);
        } catch (JobNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Job> postJob(@Validated @RequestBody Job job) {
        Job result = jobRepository.save(job);
        return ResponseEntity.ok().body(result);
    }


}
