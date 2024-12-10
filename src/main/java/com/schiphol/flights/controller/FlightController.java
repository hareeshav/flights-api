package com.schiphol.flights.controller;

import com.schiphol.flights.dto.FlightFilterRequest;
import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<FlightFilterResponse>> getFlights(@Valid @ModelAttribute FlightFilterRequest flightFilterRequest) {

        PaginatedResponse<FlightFilterResponse> flights = flightService.searchFlights(
                flightFilterRequest.getDestination(),
                flightFilterRequest.getStartScheduleDateTime(),
                flightFilterRequest.getEndScheduleDateTime(),
                flightFilterRequest.getFlightDirection(),
                flightFilterRequest.getMinDelayInMinutes(), flightFilterRequest.getPage(), flightFilterRequest.getPageSize()
        );
        return ResponseEntity.ok(flights);
    }
}
