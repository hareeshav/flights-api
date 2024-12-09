package com.schiphol.flights.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticsearchUrl;



    // Register custom conversions here
    @Bean
    public ElasticsearchCustomConversions customConversions() {
        return new ElasticsearchCustomConversions(
                List.of(
                        new LocalDateTimeToLongConverter(),
                        new LongToLocalDateTimeConverter()
                )
        );
    }

    // Converter to transform LocalDateTime to Long (epoch milliseconds)
    public static class LocalDateTimeToLongConverter implements Converter<LocalDateTime, Long> {
        @Override
        public Long convert(LocalDateTime source) {
            return source.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    }

    // Converter to transform Long (epoch milliseconds) to LocalDateTime
    public static class LongToLocalDateTimeConverter implements Converter<Long, LocalDateTime> {
        @Override
        public LocalDateTime convert(Long source) {
            return Instant.ofEpochMilli(source).atZone(ZoneOffset.UTC).toLocalDateTime();
        }
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        // Create and return the ElasticsearchClient
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}


