package com.schiphol.flights;

import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import com.schiphol.flights.repository.FlightRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
public class FlightFilterIntegrationTest {

    @Autowired
    private static FlightRepository flightRepository;

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

    @BeforeAll
    public static void setupElasticsearchData() {
        // Prepopulate the Elasticsearch index with sample data for testing
        List<Flight> sampleFlights = List.of(
                /*new Flight("1", "KLM", "AMS", "JFK", LocalDateTime..minusDays(1), FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 10),
                new Flight("2", "KLM", "LHR", "JFK", LocalDateTime.now(), FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 15),
                new Flight("3", "AF", "LHR", "JFK", LocalDateTime.now().plusDays(1), FlightDirection.ARRIVAL, FlightStatus.SCHEDULED, 5),
                new Flight("4", "AF", "JFK", "AMS", LocalDateTime.now().plusDays(2), FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 25),
                new Flight("5", "BA", "AMS", "LHR", LocalDateTime.now().plusDays(1), FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 20)*/
        );

        flightRepository.saveAll(sampleFlights);
    }

    @Test
    public void testPaginationAndFiltering() {
        // Fetch filtered and paginated results
        PaginatedResponse<Flight> response = flightRepository.findFlights(
                "JFK",                             // Filter by destination
                LocalDateTime.now().minusDays(2),  // Date range start
                LocalDateTime.now().plusDays(2),   // Date range end
                FlightDirection.DEPARTURE,         // Filter by flight direction
                10,                                 // Minimum delay in minutes
                0,                                  // Page
                2                                   // Size
        );

        Assertions.assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals(2, response.getTotalHits());
    }

    @Test
    public void testNoResults() {
        // Attempt to query for data with filters that don't match anything
        PaginatedResponse<Flight> response = flightRepository.findFlights(
                "ABC",                             // Non-existent destination
                LocalDateTime.now().minusDays(2),  // Date range start
                LocalDateTime.now().plusDays(2),   // Date range end
                FlightDirection.DEPARTURE,         // Filter by flight direction
                100,                                // Minimum delay in minutes
                0,                                  // Page
                2                                   // Size
        );

        Assertions.assertNotNull(response);
        assertEquals(0, response.getItems().size());
        assertEquals(0, response.getTotalHits());
    }

    @Test
    public void testFilterByDelayAndRange() {
        // Test for data where delay is >= a certain threshold
        PaginatedResponse<Flight> response = flightRepository.findFlights(
                "JFK",                             // Destination filter
                LocalDateTime.now().minusDays(1),  // Date range start
                LocalDateTime.now().plusDays(2),   // Date range end
                null,                               // No specific flight direction filter
                15,                                 // Minimum delay in minutes
                0,                                  // Page
                10                                  // Size
        );

        Assertions.assertNotNull(response);
        assertEquals(2, response.getItems().size()); // Should match based on delay values
    }
}
