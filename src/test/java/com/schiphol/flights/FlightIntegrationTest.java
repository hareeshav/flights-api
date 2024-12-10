package com.schiphol.flights;

import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import com.schiphol.flights.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class FlightIntegrationTest {

    @Autowired
    private FlightRepository flightRepository;

    // Initialize the Elasticsearch container for all tests
    @Container
    private static final org.testcontainers.elasticsearch.ElasticsearchContainer elasticsearchContainer =
            new org.testcontainers.elasticsearch.ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.10.2")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .waitingFor(Wait.forHttp("/").forPort(9200).withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Dynamically inject the container's HTTP endpoint into Spring Boot's properties
        registry.add("spring.elasticsearch.uris", () -> "http://localhost:" + elasticsearchContainer.getMappedPort(9200));
    }

    @Test
    public void testElasticsearchContainerIsRunning() {
        assertThat(elasticsearchContainer.isRunning()).isTrue();
    }

    @Test
    public void testSaveAndRetrieveFlight() {
        // Save data to Elasticsearch
        Flight flight = new Flight(
                "1",
                "KLM",
                "AMS",
                "JFK",
                System.currentTimeMillis(),
                FlightDirection.DEPARTURE,
                FlightStatus.SCHEDULED,
                10,""
        );

        flightRepository.save(flight);

        // Search for saved flight
        boolean exists = flightRepository.findById("1").isPresent();
        assertThat(exists).isTrue();
    }

    @Test
    public void testFlightNotFound() {
        // Ensure querying for non-existent records returns no results
        boolean flightExists = flightRepository.findById("non-existent-id").isPresent();
        assertThat(flightExists).isFalse();
    }

}
