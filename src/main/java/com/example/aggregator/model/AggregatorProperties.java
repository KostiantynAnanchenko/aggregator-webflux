package com.example.aggregator.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    /**
     * Default cache TTL in minutes if not configured externally.
     */
    private long cacheTtlMinutes = 1L;

    private List<Source> sources;

    @Data
    public static class Source {
        private String name;
        private String url;
    }
}