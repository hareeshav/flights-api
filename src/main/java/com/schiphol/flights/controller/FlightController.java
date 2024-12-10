package com.schiphol.flights.controller;

import com.schiphol.flights.dto.FlightFilterRequest;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<Flight>> getFlights(@Valid @ModelAttribute FlightFilterRequest flightFilterRequest) {

        PaginatedResponse<Flight> flights = flightService.searchFlights(
                flightFilterRequest.getDestination(),
                flightFilterRequest.getStartScheduleDateTime() != null ? flightFilterRequest.getStartScheduleDateTime() : LocalDateTime.parse("1970-01-01T00:00:01"),
                flightFilterRequest.getEndScheduleDateTime() != null ? flightFilterRequest.getEndScheduleDateTime() : LocalDateTime.parse("9999-12-31T23:59:59"),
                flightFilterRequest.getFlightDirection() != null ? flightFilterRequest.getFlightDirection() : FlightDirection.ARRIVAL,
                flightFilterRequest.getMinDelayInMinutes(), flightFilterRequest.getPage(), flightFilterRequest.getPageSize()
        );
        return ResponseEntity.ok(flights);
    }
}
