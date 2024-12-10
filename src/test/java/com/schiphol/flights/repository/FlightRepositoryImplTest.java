package com.schiphol.flights.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FlightRepositoryImplTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private FlightRepositoryImpl flightRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findFlightsReturnsFlightsWhenQuerySuccessful() throws Exception {
        // Prepare mock response
        Flight sampleFlight = new Flight(
                "FL1",
                "EK1",
                "ORI1",
                "AMS",
                1733798196521L,
                FlightDirection.DEPARTURE,
                FlightStatus.SCHEDULED,
                1, ""
        );

        List<Hit<Flight>> hits = List.of(
                new Hit.Builder<Flight>()
                        .index("flights")
                        .id("FL1")
                        .score(1.0)
                        .source(sampleFlight)
                        .build()
        );

        SearchResponse<Flight> searchResponse = SearchResponse.of(r -> r
                .took(10L)
                .timedOut(false)
                .shards(s -> s
                        .total(1)
                        .successful(1)
                        .skipped(0)
                        .failed(0)
                )
                .hits(h -> h
                        .hits(hits)
                        .total(t -> t.value((long) hits.size()).relation(TotalHitsRelation.Eq))
                )
        );

        when(elasticsearchClient.search(
                any(java.util.function.Function.class),
                eq(Flight.class)
        )).thenReturn(searchResponse);

        LocalDateTime startDateTime = LocalDateTime.of(2023, 12, 10, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 12, 15, 0, 0);

        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                startDateTime,
                endDateTime,
                FlightDirection.DEPARTURE,
                1,
                0,
                10
        );

        assertNotNull(response);
        assertEquals(1, response.getTotalHits());
        assertEquals("FL1", response.getItems().getFirst().getId());
    }

    @Test
    public void findFlightsReturnsEmptyWhenNoResults() throws Exception {
        List<Hit<Flight>> hits = List.of();

        SearchResponse<Flight> searchResponse = SearchResponse.of(r -> r
                .took(10L)
                .timedOut(false)
                .shards(s -> s
                        .total(0)
                        .successful(1)
                        .skipped(0)
                        .failed(0)
                )
                .hits(h -> h
                        .hits(hits)
                        .total(t -> t.value(0).relation(TotalHitsRelation.Eq))
                )
        );

        when(elasticsearchClient.search(
                any(java.util.function.Function.class),
                eq(Flight.class)
        )).thenReturn(searchResponse);

        LocalDateTime startDateTime = LocalDateTime.of(2023, 12, 10, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 12, 15, 0, 0);

        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                "AMS",
                startDateTime,
                endDateTime,
                FlightDirection.DEPARTURE,
                1,
                0,
                10
        );

        assertNotNull(response);
        assertEquals(0, response.getTotalHits());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    public void findFlightsHandlesExceptionWhenElasticsearchFails() throws Exception {
        when(elasticsearchClient.search(
                any(java.util.function.Function.class),
                eq(Flight.class)
        )).thenThrow(new RuntimeException("Elasticsearch failure"));

        LocalDateTime startDateTime = LocalDateTime.of(2023, 12, 10, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 12, 15, 0, 0);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            flightRepository.findFlights(
                    "AMS",
                    startDateTime,
                    endDateTime,
                    FlightDirection.DEPARTURE,
                    1,
                    0,
                    10
            );
        });

        assertEquals("Error querying Elasticsearch", exception.getMessage());
    }

    @Test
    public void findFlightsWhenNoDestinationProvided() throws Exception {
        List<Hit<Flight>> hits = List.of(
                new Hit.Builder<Flight>()
                        .index("flights")
                        .id("FL1")
                        .score(1.0)
                        .source(new Flight("FL1", "EK1", "ORI1", "AMS", 1733798196521L, FlightDirection.DEPARTURE, FlightStatus.SCHEDULED, 1, ""))
                        .build()
        );

        SearchResponse<Flight> searchResponse = SearchResponse.of(r -> r
                .took(10L)
                .timedOut(false)
                .shards(s -> s
                        .total(1)
                        .successful(1)
                        .skipped(0)
                        .failed(0)
                )
                .hits(h -> h
                        .hits(hits)
                        .total(t -> t.value(1).relation(TotalHitsRelation.Eq))
                )
        );

        when(elasticsearchClient.search(
                any(java.util.function.Function.class),
                eq(Flight.class)
        )).thenReturn(searchResponse);

        LocalDateTime startDateTime = LocalDateTime.of(2023, 12, 10, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 12, 15, 0, 0);

        PaginatedResponse<FlightFilterResponse> response = flightRepository.findFlights(
                null,
                startDateTime,
                endDateTime,
                FlightDirection.DEPARTURE,
                1,
                0,
                10
        );

        assertNotNull(response);
        assertEquals(1, response.getTotalHits());
    }
}
