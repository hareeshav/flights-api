package com.schiphol.flights.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class FlightRepositoryImpl implements FlightRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(FlightRepositoryImpl.class);
    @Autowired
    private ElasticsearchClient elasticsearchClient;


    @Override
    public PaginatedResponse<Flight> findFlights(String destination, LocalDateTime startDateTime, LocalDateTime endDateTime, FlightDirection flightDirection, int minDelayInMinutes, int page, int size) {
        try {
            var response = elasticsearchClient.search(
                    s -> s.index("flights")
                            .query(q -> q.bool(b -> b
                                    .must(m -> {
                                        if (destination != null) {
                                            return m.match(mt -> mt.field("destination").query(destination));
                                        }
                                        return null;
                                    })
                                          .must(m -> {
                                                if (flightDirection != null) {
                                                    return m.match(t -> t.field("flightDirection").query(flightDirection.getValue()));
                                                }
                                                return null;
                                            })

                                   .filter(f -> {
                                        if (startDateTime != null && endDateTime != null) {
                                            return f.range(r -> r
                                                    .number(d -> d
                                                            .field("scheduleDateTime")
                                                            .gte((double)startDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                                                            .lte((double)endDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())

                                                    )
                                            );
                                        }
                                        return null;
                                    })
                                    .filter(f -> {
                                        if (minDelayInMinutes > 0) {
                                            return f.range(r -> r
                                                    .number(n -> n
                                                            .field("delayInMinutes")
                                                            .gte((double)minDelayInMinutes))
                                            );
                                        }
                                        return null;
                                    })
                            ))
                            .from(page * size)
                            .size(size),
                    Flight.class
            );

            List<Flight> flights = response.hits().hits()
                    .stream()
                    .map(Hit::source)
                    .toList();


            assert response.hits().total() != null;
            return new PaginatedResponse<>(
                    flights,
                    response.hits().total().value(), // Total hits in Elasticsearch
                    page,
                    size
            );

        } catch (Exception e) {
            log.error("Error while querying Elasticsearch", e);
            throw new RuntimeException("Error querying Elasticsearch", e);
        }
    }
}
