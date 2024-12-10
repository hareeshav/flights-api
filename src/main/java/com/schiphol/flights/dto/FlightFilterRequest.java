package com.schiphol.flights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schiphol.flights.model.FlightDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class FlightFilterRequest {
    @Schema(description = "Filter by destination", example = "Ams")
    private String destination;

    @Min(0)
    @Schema(description = "Minimum delay in minutes", example = "10", defaultValue = "0")
    private Integer minDelayInMinutes = 0;

    @Schema(description = "Start schedule date/time range filter", example = "2024-02-02T17:02:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startScheduleDateTime;

    @Schema(description = "End schedule date/time range filter", example = "2024-02-02T22:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endScheduleDateTime;

    @Schema(description = "Flight direction filter", example = "DEPARTURE")
    private FlightDirection flightDirection;

    private int page;

    private int pageSize;

    public LocalDateTime getStartScheduleDateTime() {
        return Objects.requireNonNullElse(
                startScheduleDateTime,
                LocalDateTime.parse("1970-01-01T00:00:00")
        );
    }

    public LocalDateTime getEndScheduleDateTime() {
        return Objects.requireNonNullElse(
                endScheduleDateTime,
                LocalDateTime.parse("9999-12-31T23:59:59")
        );
    }

    public FlightDirection getFlightDirection() {
        return Objects.requireNonNullElse(
                flightDirection,
                FlightDirection.DEPARTURE
        );
    }
}
