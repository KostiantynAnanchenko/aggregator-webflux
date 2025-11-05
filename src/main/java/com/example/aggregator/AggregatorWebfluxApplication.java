package com.example.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.aggregator.model")
public class AggregatorWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorWebfluxApplication.class, args);
    }

}
