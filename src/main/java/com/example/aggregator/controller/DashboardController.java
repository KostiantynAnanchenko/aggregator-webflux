package com.example.aggregator.controller;


import com.example.aggregator.service.AggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardController {

    private final AggregatorService aggregatorService;

    /**
     * GET /api/dashboard
     * Returns aggregated JSON from multiple external APIs (with caching and fault-tolerance).
     */
    @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getDashboard() {
        log.debug("Received request for /api/dashboard");

        return aggregatorService.aggregate()
                .map(json -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-Service-Version", "aggregator-v2") // updated version
                        .body(json)
                )
                .onErrorResume(e -> {
                    log.warn("Aggregation failed: {}", e.getMessage());
                    return Mono.just(
                            ResponseEntity.status(502)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                                    .body("Upstream services unavailable or cache empty")
                    );
                });
    }
}