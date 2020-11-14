package com.morova.onlab.worker;

import com.morova.onlab.worker.service.WorkerService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkerApplication {

	@Autowired
	private MeterRegistry meterRegistry;

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	@Bean
	public MeterBinder busyThreads(WorkerService workerService) {
		return meterRegistry -> {
			Gauge.builder("worker.busy.threads", workerService::getBusyThreads).register(meterRegistry);
		};
	}

}
