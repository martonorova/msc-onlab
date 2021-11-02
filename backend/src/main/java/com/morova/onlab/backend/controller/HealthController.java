package com.morova.onlab.backend.controller;

import com.morova.onlab.backend.messaging.Producer;
import com.morova.onlab.backend.model.Job;
import com.morova.onlab.backend.model.TestObject;
import com.morova.onlab.backend.repository.TestObjectRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.time.LocalDateTime;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired
    private TestObjectRepository testObjectRepository;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    Producer producer;

    private final RestTemplate restTemplate;

    private LocalDateTime lastSuccessfulExecutionTime;

    @Value("http://${worker.host}:${worker.port}/api/health")
    private String workerHealthEndpoint;

    @Value("${messaging}")
    private String messagingTech;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    private Consumer<String, String> testConsumer;

    Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    public HealthController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping
    public ResponseEntity<String> isHealthy() {

        String errorMsg = "No error";

        // test DB connection
        try {
            logger.info("[HEALTH CHECK] Checking DB connection...");
            TestObject to = testObjectRepository.save(new TestObject("Test Data", LocalDateTime.now()));
            testObjectRepository.findById(to.getId());
            logger.info("[HEALTH CHECK] DB connection OK");
        } catch (Exception ex) {
            errorMsg = "DB connection FAILURE";
            logger.info("[HEALTH CHECK] " + errorMsg);
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }

        // check if a worker is running
        logger.info("[HEALTH CHECK] Checking Worker connection...");
        ResponseEntity<String> response
                = restTemplate.getForEntity(workerHealthEndpoint, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("[HEALTH CHECK] Worker connection OK");
        } else {
            errorMsg = "Worker connection FAILURE";
            logger.info("[HEALTH CHECK] " + errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }

        if (messagingTech.equals("activemq")) {
            try {
                testActiveMqConnection();
            } catch (JMSException ex) {
                errorMsg = "ActiveMQ connection FAILURE";
                ex.printStackTrace();
                logger.info("[HEALTH CHECK] ActiveMQ connection FAILURE");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            } catch (IllegalStateException ex) {
                errorMsg = "ActiveMQ connection FAILURE - wrong text message";
                ex.printStackTrace();
                logger.info("[HEALTH CHECK] ActiveMQ connection FAILURE");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }
            logger.info("[HEALTH CHECK] ActiveMQ connection OK");
        } else if (messagingTech.equals("kafka")) {
            try {
                testKafkaConnection();
            } catch (Exception ex) {
                errorMsg = "Kafka connection FAILURE";
                ex.printStackTrace();
                logger.info("[HEALTH CHECK] " + errorMsg);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }
            logger.info("[HEALTH CHECK] Kafka connection OK");
        }



        // test workflow with sample input (if last success is older than 90)
        // if there is a not too old execution, return true
        if (lastSuccessfulExecutionTime != null
                && LocalDateTime.now().minusSeconds(90).isBefore(lastSuccessfulExecutionTime)) {
            logger.info("[HEALTH CHECK] " + "Last successful execution OK");
        }

        // if there is not any new successful execution, run it
        if (lastSuccessfulExecutionTime == null
                || LocalDateTime.now().minusSeconds(90).isAfter(lastSuccessfulExecutionTime)) {

            producer.sendJob(new Job(-1L, 1, -1L));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (lastSuccessfulExecutionTime == null
                    || LocalDateTime.now().minusSeconds(90).isAfter(lastSuccessfulExecutionTime)) {
                errorMsg = "Last successful execution FAILURE";
                logger.info("[HEALTH CHECK] " + errorMsg);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }
        }

        return ResponseEntity.ok("HEALTHY");
    }

    public void setLastSuccessfulExecutionTime(LocalDateTime lastSuccessfulExecutionTime) {
        this.lastSuccessfulExecutionTime = lastSuccessfulExecutionTime;
    }

    private void testActiveMqConnection() throws JMSException {
        // check activeMQ connection
        logger.info("[HEALTH CHECK] Checking ActiveMQ connection...");

        // Produce test message

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


        // Consume test message

        // Create Connection
        connection = connectionFactory.createConnection();
        connection.start();
        // Create Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create Destination
        destination = session.createQueue("testQueue");
        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);
        // Wait for a message
        Message messageReceived = consumer.receive(10000);

        if (messageReceived instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) messageReceived;
            text = textMessage.getText();
            if (text.equals("testMessage")) {
                logger.info("[HEALTH CHECK] ActiveMQ connection OK");
            } else {
                throw new IllegalStateException("Text was " + text);
            }
        } else {
            throw new IllegalStateException("messageReceived was not of type TextMessage");
        }

        consumer.close();
        session.close();
        connection.close();
    }

    private void testKafkaConnection() {
        // check Kafka connection
        logger.info("[HEALTH CHECK] Checking Kafka connection...");

        testConsumer = consumerFactory.createConsumer();

        testConsumer.listTopics();
    }
}
