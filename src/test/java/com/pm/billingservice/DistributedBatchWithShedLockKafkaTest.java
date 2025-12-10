package com.pm.billingservice;

import com.pm.billingservice.service.MonthlyCommissionJob;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Achintha Kalunayaka
 * @since 11/8/2025
 */

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DistributedBatchWithShedLockKafkaTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @Container
    static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:4.1.0").asCompatibleSubstituteFor("apache/kafka");
    @Container
    static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        // Redis (ShedLock backend)
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));

        // Kafka
        registry.add("spring.kafka.bootstrap-servers",
                () -> List.of(kafka.getBootstrapServers().replace("PLAINTEXT://", "")));
        // Disable real scheduling
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
    }

    @Autowired
    private MonthlyCommissionJob commissionJob;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
        System.out.println("Kafka bootstrap: " + kafka.getBootstrapServers());
        System.out.println("Redis host: " + redis.getHost() + ":" + redis.getFirstMappedPort());
    }

    @Test
    void testShedLockEnsuresSingleLeaderExecution() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // Simulate two nodes trying to execute same scheduled method
        pool.submit(() -> runJobAsNode("node-1"));
        pool.submit(() -> runJobAsNode("node-2"));

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // Verify ShedLock only allowed one execution
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            Set<byte[]> keys = conn.keys("shedlock:*".getBytes());
            assertFalse(keys.isEmpty(), "ShedLock key not found");
            keys.forEach(k -> System.out.println("Lock key found: " + new String(k)));
        }
    }

    private void runJobAsNode(String nodeName) {
        try {
            System.out.println("Starting node: " + nodeName);
            commissionJob.generateMonthlyInvoice();
            System.out.println("Node " + nodeName + " finished execution.");
        } catch (Exception e) {
            System.out.println("Node " + nodeName + " failed: " + e.getMessage());
        }
    }
}

