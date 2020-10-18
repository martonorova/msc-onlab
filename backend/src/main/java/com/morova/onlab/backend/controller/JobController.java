package com.morova.onlab.backend.controller;

import com.morova.onlab.backend.exception.JobNotFoundException;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    private final RestTemplate restTemplate;
    private final String workerUrl = "http://localhost:5000/api/jobs";

    public JobController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


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

        Job uncompleted = jobRepository.save(job);

        Job result = sendJob(uncompleted);

        Job resultToUpdate = jobRepository
                            .findById(uncompleted.getId())
                            .orElseThrow(() -> new JobNotFoundException(uncompleted.getId()));
        resultToUpdate.setResult(result.getResult());
        Job savedResult = jobRepository.save(resultToUpdate);

        return ResponseEntity.ok().body(savedResult);
    }

    private Job sendJob(Job job) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Job> entity = new HttpEntity<>(job, headers);

        return restTemplate.postForObject(workerUrl, entity, Job.class);
    }


}
