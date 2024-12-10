package com.schiphol.flights.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FlightStatus {
    SCHEDULED("SCHEDULED"), DEPARTED("DEPARTED"), ARRIVED("ARRIVED"), CANCELLED("CANCELLED");
    private final String value;

    FlightStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FlightStatus fromString(String value) {
        for (FlightStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
