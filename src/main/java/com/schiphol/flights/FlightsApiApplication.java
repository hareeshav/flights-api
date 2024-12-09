package com.schiphol.flights;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Flights API", version = "1.0", description = "API for managing and querying flight data"))
public class FlightsApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(FlightsApiApplication.class, args);
	}
}
