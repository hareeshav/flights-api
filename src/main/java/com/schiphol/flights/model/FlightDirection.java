package com.schiphol.flights.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FlightDirection {
    DEPARTURE("DEPARTURE"),
    ARRIVAL("ARRIVAL");

    private final String value;

    FlightDirection(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FlightDirection fromString(String value) {
        for (FlightDirection direction : values()) {
            if (direction.value.equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

