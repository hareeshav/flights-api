package com.schiphol.flights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schiphol.flights.model.FlightDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightFilterRequest {
    @Schema(description = "Filter by destination", example = "Ams")
    private String destination;

    @Min(0)
    @Schema(description = "Minimum delay in minutes", example = "10")
    private Integer minDelayInMinutes;

    @Schema(description = "Start schedule date/time range filter", example = "2023-12-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startScheduleDateTime;

    @Schema(description = "End schedule date/time range filter", example = "2023-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endScheduleDateTime;

    @Schema(description = "Flight direction filter", example = "DEPARTURE")
    private FlightDirection flightDirection;

    private int page;

    private int pageSize;
}
