package com.morova.onlab.backend.repository;

import com.morova.onlab.backend.model.TestObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestObjectRepository extends JpaRepository<TestObject, Long> {
}
