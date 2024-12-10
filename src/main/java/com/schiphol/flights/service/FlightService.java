package com.schiphol.flights.service;

import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.repository.FlightRepositoryCustom;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepositoryCustom flightRepository;

    public FlightService(FlightRepositoryCustom flightRepository) {
        this.flightRepository = flightRepository;
    }

    public PaginatedResponse<Flight> searchFlights(
            String destination,
            LocalDateTime start,
            LocalDateTime end,
            FlightDirection direction,
            int minDelay, int page, int size
    ) {
        return flightRepository.findFlights(
                destination,
                start != null ? start : LocalDateTime.parse("1970-01-01T00:00:00"),
                end != null ? end : LocalDateTime.parse("9999-12-31T23:59:59"),
                direction,
                minDelay,
                page, size
        );
    }
}
