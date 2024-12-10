package com.schiphol.flights.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Repository
public class FlightRepositoryImpl implements FlightRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(FlightRepositoryImpl.class);
    @Autowired
    private ElasticsearchClient elasticsearchClient;


    @Override
    public PaginatedResponse<FlightFilterResponse> findFlights(String destination, LocalDateTime startDateTime, LocalDateTime endDateTime, FlightDirection flightDirection, int minDelayInMinutes, int page, int size) {
        try {
            log.info("Querying ES for results");
            var response = elasticsearchClient.search(
                    s -> s.index("flights")
                            .query(q -> q.bool(b -> {
                                // Create a dynamic Boolean query
                                List<co.elastic.clients.elasticsearch._types.query_dsl.Query> mustConditions = new ArrayList<>();
                                List<co.elastic.clients.elasticsearch._types.query_dsl.Query> filterConditions = new ArrayList<>();

                                if (destination != null) {
                                    mustConditions.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(m -> m.match(mt -> mt.field("destination").query(destination))));
                                }

                                if (flightDirection != null) {
                                    mustConditions.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(m -> m.match(mt -> mt.field("flightDirection").query(flightDirection.getValue()))));
                                }

                                if (startDateTime != null && endDateTime != null) {
                                    filterConditions.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(f -> f.range(r -> r
                                            .number(d -> d
                                                    .field("scheduleDateTime")
                                                    .gte((double) startDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                                                    .lte((double) endDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                                            )
                                    )));
                                }

                                if (minDelayInMinutes > 0) {
                                    filterConditions.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(f -> f.range(r -> r
                                            .number(d -> d
                                                    .field("delayInMinutes")
                                                    .gte((double) minDelayInMinutes)
                                            )
                                    )));
                                }

                                // Return the complete boolean query
                                return b
                                        .must(mustConditions)
                                        .filter(filterConditions);
                            }))
                            .from(page * size)
                            .size(size),
                    Flight.class
            );

            // Transform Flight entities to FlightResponse DTOs
            List<FlightFilterResponse> flightResponses = response.hits().hits()
                    .stream()
                    .map(Hit::source).filter(Objects::nonNull)
                    .map(FlightFilterResponse::new)
                    .toList();


            assert response.hits().total() != null;
            return new PaginatedResponse<>(
                    flightResponses,
                    response.hits().total().value(),
                    page,
                    size
            );

        } catch (Exception e) {
            log.error("Error while querying Elasticsearch", e);
            throw new RuntimeException("Error querying Elasticsearch", e);
        }
    }

}
