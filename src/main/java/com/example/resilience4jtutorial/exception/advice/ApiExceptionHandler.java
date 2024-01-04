package com.example.resilience4jtutorial.exception.advice;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.springboot3.bulkhead.monitoring.endpoint.BulkheadEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({CallNotPermittedException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleCallNotPermittedException() {
        System.out.println("circuit is fully opened!!");
    }

    @ExceptionHandler({TimeoutException.class})
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public void handleTimeoutException() {
        System.out.println("time out!!");
    }
}
