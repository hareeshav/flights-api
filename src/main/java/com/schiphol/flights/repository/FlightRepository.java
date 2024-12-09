package com.schiphol.flights.repository;

import com.schiphol.flights.model.Flight;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FlightRepository extends ElasticsearchRepository<Flight, String> , FlightRepositoryCustom{}
