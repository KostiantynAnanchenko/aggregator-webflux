package com.example.aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Aggregates data from multiple external APIs concurrently,
 * caches results in Redis, and provides fault-tolerant responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    private static final String CACHE_KEY = "dashboard:cache";

    private final WebClient webClient;
    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AggregatorProperties properties;

    public Mono<String> aggregate() {
        return Flux.fromIterable(properties.getSources())
                .flatMap(source -> fetchJson(source.getUrl())
                        .map(json -> Map.entry(source.getName(), json))
                        .onErrorResume(e -> {
                            log.warn("Failed to fetch {}: {}", source.getName(), e.getMessage());
                            return Mono.empty();
                        })
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .flatMap(map -> {
                    ObjectNode root = objectMapper.createObjectNode();
                    map.forEach(root::set);
                    String json = root.toString();

                    return redis.opsForValue()
                            .set(CACHE_KEY, json, CACHE_TTL)
                            .thenReturn(json)
                            .onErrorResume(e -> {
                                log.error("Redis unavailable, skipping cache: {}", e.getMessage());
                                return Mono.just(json);
                            });
                })
                .switchIfEmpty(
                        redis.opsForValue().get(CACHE_KEY)
                                .switchIfEmpty(Mono.error(new RuntimeException("No data and cache empty")))
                );
    }

    private Mono<JsonNode> fetchJson(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson);
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("JSON parsing failed", e);
        }
    }
}