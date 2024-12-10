package com.schiphol.flights.dto;

import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Data
public class FlightFilterResponse {
    private String id;
    private String airlineCode;
    private String origin;
    private String destination;
    private String scheduleDateTime; // Format as a string
    private FlightDirection flightDirection;
    private FlightStatus status;
    private int delayInMinutes;

    public FlightFilterResponse(Flight flight) {
        this.id = flight.getId();
        this.airlineCode = flight.getAirlineCode();
        this.origin = flight.getOrigin();
        this.destination = flight.getDestination();
        this.scheduleDateTime = Instant.ofEpochMilli(flight.getScheduleDateTime())
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        this.flightDirection = flight.getFlightDirection();
        this.status = flight.getStatus();
        this.delayInMinutes = flight.getDelayInMinutes();
    }

}

