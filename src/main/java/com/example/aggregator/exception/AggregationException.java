package com.example.aggregator.exception;

public class AggregationException extends RuntimeException {
    public AggregationException(String message, Throwable cause) {
        super(message, cause);
    }
}