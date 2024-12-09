package com.schiphol.flights.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class FlightRepositoryImpl implements FlightRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(FlightRepositoryImpl.class);
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public List<Flight> findFlights(String destination, LocalDateTime startDateTime, LocalDateTime endDateTime, FlightDirection flightDirection, int minDelayInMinutes) {
        try{
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        if(null != destination) {
            boolQueryBuilder.must(b-> b.term(t-> t.field("destination").value(destination)));
        }
        if(flightDirection != null) {
            boolQueryBuilder.must(b -> b.term(t -> t.field("flightDirection").value(flightDirection.toString())));
        }
        // Execute the search
        SearchResponse<Flight> searchResponse  = elasticsearchClient.search(
                    s -> s.index("flights")
                            .query(q -> q.bool((Function<BoolQuery.Builder, ObjectBuilder<BoolQuery>>) boolQueryBuilder))
                            .size(100),
                    Flight.class
            );


        // Map search hits to Flight objects
        return searchResponse.hits().hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error while querying Elastic Search", e);
            throw new RuntimeException(e);
        }
    }
}
