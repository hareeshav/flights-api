package com.schiphol.flights;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.JsonData;
import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import com.schiphol.flights.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@SpringBootTest
@Testcontainers
@Slf4j
public class FlightFilterIntegrationTest {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.10.2")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .waitingFor(Wait.forHttp("/").forPort(9200).withStartupTimeout(Duration.ofSeconds(60)));
    private static List<Flight> sampleFlights;

    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.rest.uris", () -> "http://localhost:" + elasticsearchContainer.getMappedPort(9200));
    }

    @BeforeAll
    public static void setupSampleData() {
        sampleFlights = List.of(
                new Flight("1", "KLM", "AMS", "JFK",
                        LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 10, ""),
                new Flight("2", "KLM", "LHR", "JFK",
                        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 15, ""),
                new Flight("3", "AF", "LHR", "JFK",
                        LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        FlightDirection.ARRIVAL, FlightStatus.SCHEDULED, 5, ""),
                new Flight("4", "AF", "JFK", "AMS",
                        LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 25, ""),
                new Flight("5", "BA", "AMS", "LHR",
                        LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 20, "")
        );
    }

    @BeforeEach
    public void setupIndexAndData() throws Exception {
        clearIndex();
        createIndexIfNotExists();
        indexTestData();
        Thread.sleep(1000); // Ensure Elasticsearch indexes are ready
    }

    private void clearIndex() {
        try {
            elasticsearchClient.indices().delete(d -> d.index("flights"));
            log.info("Cleared Elasticsearch index.");
        } catch (Exception e) {
            log.info("Index already absent or cleared.");
        }
    }

    private void createIndexIfNotExists() {
        try {
            String mapping = """
                    {
                        "mappings": {
                            "properties": {
                                "scheduleDateTime": { "type": "long" },
                                "delayInMinutes": { "type": "integer" },
                                "flightDirection": { "type": "keyword" },
                                "status": { "type": "keyword" },
                                "origin": { "type": "keyword" },
                                "destination": { "type": "keyword" },
                                "id": { "type": "keyword" }
                            }
                        }
                    }
                    """;

            CreateIndexResponse response = elasticsearchClient.indices()
                    .create(c -> c.index("flights").mappings((TypeMapping) JsonData.of(mapping)));
            if (response.acknowledged()) {
                log.info("Successfully created index.");
            }
        } catch (Exception e) {
            log.error("Error during index creation", e);
        }
    }

    private void indexTestData() {
        sampleFlights.forEach(flight -> {
            try {
                elasticsearchClient.index(i -> i
                        .index("flights")
                        .id(flight.getId())
                        .document(flight));
            } catch (Exception e) {
                log.error("Error indexing data: ", e);
            }
        });
    }

    @Test
    public void testPaginationAndFiltering() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "JFK",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                1,
                0,
                2
        );

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.getItems().size());
    }

    @Test
    public void testFilterByFlightDirection() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                0,
                0,
                2
        );

        Assertions.assertEquals(1, response.getItems().size());
    }

    @Test
    public void testFilterByDestinationOnly() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "JFK",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                0,
                0,
                2
        );

        Assertions.assertEquals(2, response.getItems().size());
    }

    @Test
    public void testFilterByDelayInMinutes() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                15,
                0,
                2
        );

        Assertions.assertEquals(1, response.getItems().size());
    }

    @Test
    public void testFilterByDateRangeOnly() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(1),
                FlightDirection.DEPARTURE,
                0,
                0,
                2
        );

        Assertions.assertEquals(0, response.getItems().size());
    }

    @Test
    public void testCombinedFilters() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(5),
                FlightDirection.DEPARTURE,
                15,
                0,
                2
        );

        Assertions.assertEquals(1, response.getItems().size());
    }

    @Test
    public void testNoMatchingResults() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "NonexistentDestination",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                20,
                0,
                2
        );

        Assertions.assertEquals(0, response.getItems().size());
    }

    @Test
    public void testEmptyPaginationResponse() {
        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                FlightDirection.DEPARTURE,
                0,
                10,
                100
        );

        Assertions.assertEquals(0, response.getItems().size());
    }
}
