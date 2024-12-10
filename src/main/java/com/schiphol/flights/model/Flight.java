package com.schiphol.flights.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "flights")
public class Flight {

    @Id
    @NotBlank
    @JsonProperty("id")
    private String id;

    @NotBlank
    @JsonProperty("airlineCode")
    private String airlineCode;

    @NotBlank
    @JsonProperty("origin")
    private String origin;

    @NotBlank
    @JsonProperty("destination")
    private String destination;

    @NotNull
    @JsonProperty("scheduleDateTime")
    private Long scheduleDateTime;

    @NotNull
    @JsonProperty("flightDirection")
    private FlightDirection flightDirection;

    @NotNull
    @JsonProperty("status")
    private FlightStatus status;

    @PositiveOrZero
    @JsonProperty("delayInMinutes")
    private int delayInMinutes;


    @JsonIgnore
    private String _class;
}
