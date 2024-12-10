package com.schiphol.flights.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
public class ElasticSearchConfig {

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
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(@Value("${spring.elasticsearch.rest.uris}") String elasticsearchUrl, ObjectMapper objectMapper) {
        try {
            // Parse the mapped port URL properly
            URI uri = new URI(elasticsearchUrl);
            RestClient restClient = RestClient.builder(
                    new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())
            ).build();

            ElasticsearchTransport transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper(objectMapper)
            );

            return new ElasticsearchClient(transport);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error parsing Elasticsearch URL", e);
        }
    }

}


