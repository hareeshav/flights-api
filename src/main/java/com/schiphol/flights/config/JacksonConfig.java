package com.schiphol.flights.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder configureJackson() {
        return new Jackson2ObjectMapperBuilder().modules(new JavaTimeModule());
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customize() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        };
    }

}
