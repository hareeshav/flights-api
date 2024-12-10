package com.schiphol.flights.controller;

import com.schiphol.flights.dto.FlightFilterRequest;
import com.schiphol.flights.dto.FlightFilterResponse;
import com.schiphol.flights.dto.PaginatedResponse;
import com.schiphol.flights.service.FlightService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flights")
@Slf4j
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /**
     * Handles HTTP GET requests to retrieve a paginated list of flights based on filter criteria.
     * <p>
     * This endpoint allows clients to search for flights with optional filters such as destination,
     * schedule date/time range, flight direction, minimum delay, and pagination parameters.
     *
     * @param flightFilterRequest The filter criteria and pagination parameters encapsulated in a request object.
     * @return A ResponseEntity containing the paginated response with a list of matching FlightFilterResponse objects.
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<FlightFilterResponse>> getFlights(@Valid @ModelAttribute FlightFilterRequest flightFilterRequest) {
        log.info("Received request to fetch flights with filters");
        PaginatedResponse<FlightFilterResponse> flights = flightService.searchFlights(
                flightFilterRequest.getDestination(),
                flightFilterRequest.getStartScheduleDateTime(),
                flightFilterRequest.getEndScheduleDateTime(),
                flightFilterRequest.getFlightDirection(),
                flightFilterRequest.getMinDelayInMinutes(), flightFilterRequest.getPage(), flightFilterRequest.getPageSize()
        );
        log.info("Returning response to client.");
        return ResponseEntity.ok(flights);
    }
}
