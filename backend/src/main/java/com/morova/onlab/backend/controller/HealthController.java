package com.morova.onlab.backend.controller;

import com.morova.onlab.backend.messaging.JMSProducer;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.model.TestObject;
import com.morova.onlab.backend.repository.TestObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.jms.*;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired
    private TestObjectRepository testObjectRepository;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    JMSProducer jmsProducer;

    private final RestTemplate restTemplate;

    private LocalDateTime lastSuccessfulExecutionTime;

    @Value("http://${worker.host}:${worker.port}/api/health")
    private String workerHealthEndpoint;

    public HealthController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping
    public ResponseEntity<String> isHealthy() {

        String errorMsg = "No error";

        // test DB connection
        try {
            System.out.println("[HEALTH CHECK] Checking DB connection...");
            TestObject to = testObjectRepository.save(new TestObject("Test Data", LocalDateTime.now()));
            testObjectRepository.findById(to.getId());
            System.out.println("[HEALTH CHECK] DB connection OK");
        } catch (Exception ex) {
            errorMsg = "DB connection FAILURE";
            System.out.println("[HEALTH CHECK] " + errorMsg);
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }

        // check if a worker is running
        System.out.println("[HEALTH CHECK] Checking Worker connection...");
        ResponseEntity<String> response
                = restTemplate.getForEntity(workerHealthEndpoint, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("[HEALTH CHECK] Worker connection OK");
        } else {
            errorMsg = "Worker connection FAILURE";
            System.out.println("[HEALTH CHECK] " + errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }

        // check activeMQ connection
        System.out.println("[HEALTH CHECK] Checking ActiveMQ connection...");

        // Produce test message
        try {
            // Create Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            //Create Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Create Destination
            Destination destination = session.createQueue("testQueue");
            // Create MessageProducer
            MessageProducer messageProducer = session.createProducer(destination);
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // Create a message
            String text = "testMessage";
            TextMessage message = session.createTextMessage(text);
            // Send message
            messageProducer.send(message);
            // Clean up
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

        // Consume test message
        try {
            // Create Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            // Create Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Create Destination
            Destination destination = session.createQueue("testQueue");
            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);
            // Wait for a message
            Message message = consumer.receive(10000);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                if (text.equals("testMessage")) {
                    System.out.println("[HEALTH CHECK] ActiveMQ connection OK");
                } else {
                    errorMsg = "ActiveMQ connection FAILURE";
                    System.out.println("[HEALTH CHECK] " + errorMsg);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
                }
//                System.out.println("Received: " + text);
            } else {
//                System.out.println("Received: " + message);
                errorMsg = "ActiveMQ connection FAILURE";
                System.out.println("[HEALTH CHECK] " + errorMsg);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }

            consumer.close();
            session.close();
            connection.close();

        } catch (JMSException ex) {
            errorMsg = "ActiveMQ connection FAILURE";
            ex.printStackTrace();
            System.out.println("[HEALTH CHECK] ActiveMQ connection FAILURE");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }


        // test workflow with sample input (if last success is older than 90)
        // if there is a not too old execution, return true
        if (lastSuccessfulExecutionTime != null
                && LocalDateTime.now().minusSeconds(90).isBefore(lastSuccessfulExecutionTime)) {
            System.out.println("[HEALTH CHECK] " + "Last successful execution OK");
        }

        // if there is not any new successful execution, run it
        if (lastSuccessfulExecutionTime == null
                || LocalDateTime.now().minusSeconds(90).isAfter(lastSuccessfulExecutionTime)) {

            jmsProducer.sendJob(new Job(-1L, 1, -1L));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (lastSuccessfulExecutionTime == null
                    || LocalDateTime.now().minusSeconds(90).isAfter(lastSuccessfulExecutionTime)) {
                errorMsg = "Last successful execution FAILURE";
                System.out.println("[HEALTH CHECK] " + errorMsg);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }
        }

        return ResponseEntity.ok("HEALTHY");
    }

    public void setLastSuccessfulExecutionTime(LocalDateTime lastSuccessfulExecutionTime) {
        this.lastSuccessfulExecutionTime = lastSuccessfulExecutionTime;
    }
}
