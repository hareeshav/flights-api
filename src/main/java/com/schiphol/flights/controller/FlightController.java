package com.schiphol.flights.controller;

import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.repository.FlightRepository;
import com.schiphol.flights.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<Flight>> getFlights(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDateTime startDateTime,
            @RequestParam(required = false) LocalDateTime endDateTime,
            @RequestParam(required = false) FlightDirection flightDirection,
            @RequestParam(required = false, defaultValue = "0") int minDelayInMinutes) {

        List<Flight> flights = flightService.searchFlights(
                destination,
                startDateTime != null ? startDateTime : LocalDateTime.parse("1970-01-01T00:00:00"),
                endDateTime != null ? endDateTime : LocalDateTime.parse("9999-12-31T23:59:59"),
                flightDirection != null ? flightDirection : FlightDirection.ARRIVAL,
                minDelayInMinutes
        );
        return ResponseEntity.ok(flights);
    }
}
