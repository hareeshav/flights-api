package com.schiphol.flights.service;

import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.repository.FlightRepositoryCustom;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FlightService {

    private final FlightRepositoryCustom flightRepository;

    public FlightService(FlightRepositoryCustom flightRepository) {
        this.flightRepository = flightRepository;
    }

    public PaginatedResponse<FlightFilterResponse> searchFlights(
            String destination,
            LocalDateTime start,
            LocalDateTime end,
            FlightDirection direction,
            int minDelay, int page, int size
    ) {
        return flightRepository.findFlights(
                destination,
                start,
                end,
                direction,
                minDelay,
                page, size
        );
    }
}
