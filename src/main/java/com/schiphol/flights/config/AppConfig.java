package com.schiphol.flights.config;

import com.schiphol.flights.model.Flight;
import com.schiphol.flights.model.FlightDirection;
import com.schiphol.flights.model.FlightStatus;
import com.schiphol.flights.repository.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Configuration
public class AppConfig {
    @Bean
    CommandLineRunner initializeDatabase(FlightRepository flightRepository) {
        return args -> {
            if (flightRepository.count() == 0) {
                List<Flight> flights = IntStream.range(1, 5001).mapToObj(i ->
                        new Flight(
                                "FL" + i,
                                "EK" + (i % 10),
                                "ORI" + (i % 5),
                                "DST" + (i % 7),
                                LocalDateTime.now().plusHours(i % 24),
                                FlightDirection.values()[i % FlightDirection.values().length],
                                FlightStatus.values()[i % FlightStatus.values().length],
                                i % 60
                        )
                ).toList();
                flightRepository.saveAll(flights);
            }
        };
    }
}