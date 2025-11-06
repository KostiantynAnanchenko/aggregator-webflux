package com.example.aggregator;

import com.example.aggregator.model.AggregatorProperties;
import com.example.aggregator.service.AggregatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AggregatorServiceTest {

	private AggregatorService service;
	private WebClient webClient;
	private ReactiveStringRedisTemplate redis;
	private ObjectMapper objectMapper;
	private AggregatorProperties props;
	private ReactiveValueOperations<String, String> valueOps;

	@BeforeEach
	void setup() {
		webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
		redis = mock(ReactiveStringRedisTemplate.class);
		valueOps = mock(ReactiveValueOperations.class);
		when(redis.opsForValue()).thenReturn(valueOps);

		objectMapper = new ObjectMapper();
		props = new AggregatorProperties();
		AggregatorProperties.Source s1 = new AggregatorProperties.Source();
		s1.setName("ip");
		s1.setUrl("https://api.ipify.org/?format=json");
		props.setSources(List.of(s1));

		service = new AggregatorService(webClient, redis, objectMapper, props);
	}

	@Test
	void shouldAggregateSuccessfully() {
		// given
		String apiResponse = "{\"ip\":\"127.0.0.1\"}";
		when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
				.thenReturn(Mono.just(apiResponse));
		when(valueOps.set(anyString(), anyString(), any()))
				.thenReturn(Mono.just(true));

		// when
		Mono<String> result = service.aggregate();

		// then
		StepVerifier.create(result)
				.expectNextMatches(json -> json.contains("\"ip\""))
				.verifyComplete();
	}

	@Test
	void shouldReturnCachedValueOnError() {
		// given: API fails, but cache exists
		when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
				.thenReturn(Mono.error(new RuntimeException("API down")));
		when(valueOps.get(anyString()))
				.thenReturn(Mono.just("{\"cached\":true}"));

		// when
		Mono<String> result = service.aggregate();

		// then
		StepVerifier.create(result)
				.expectNext("{\"cached\":true}")
				.verifyComplete();
	}

	@Test
	void shouldThrowIfNoCacheAndApiFails() {
		when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
				.thenReturn(Mono.error(new RuntimeException("API down")));
		when(valueOps.get(anyString()))
				.thenReturn(Mono.empty());

		Mono<String> result = service.aggregate();

		StepVerifier.create(result)
				.expectErrorMatches(e -> e instanceof RuntimeException &&
						e.getMessage().contains("No data and cache empty"))
				.verify();
	}
}

