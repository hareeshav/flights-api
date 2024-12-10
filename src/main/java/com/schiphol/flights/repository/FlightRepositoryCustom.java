package com.schiphol.flights.repository;

import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.FlightDirection;

import java.time.LocalDateTime;

public interface FlightRepositoryCustom {
    PaginatedResponse<FlightFilterResponse> findFlights(String destination, LocalDateTime startDateTime, LocalDateTime endDateTime, FlightDirection flightDirection, int minDelayInMinutes, int page, int size);

}
