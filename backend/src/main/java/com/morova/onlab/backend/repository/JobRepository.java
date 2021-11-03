package com.morova.onlab.backend.repository;

import com.morova.onlab.backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE j.result IS NULL")
    List<Job> findUnfinishedJobs();

    @Query("SELECT j FROM Job j WHERE j.result IS NOT NULL")
    List<Job> findFinishedJobs();
}
