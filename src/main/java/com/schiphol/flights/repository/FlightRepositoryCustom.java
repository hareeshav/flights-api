package com.schiphol.flights.repository;

import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepositoryCustom {
    PaginatedResponse<Flight> findFlights(String destination, LocalDateTime startDateTime, LocalDateTime endDateTime, FlightDirection flightDirection, int minDelayInMinutes, int page, int size);

}
