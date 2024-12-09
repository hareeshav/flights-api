package com.schiphol.flights_api;

import org.springframework.boot.SpringApplication;

public class TestFlightsApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlightsApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
