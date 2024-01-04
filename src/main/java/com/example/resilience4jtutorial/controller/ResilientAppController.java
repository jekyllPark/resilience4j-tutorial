package com.example.resilience4jtutorial.controller;

import com.example.resilience4jtutorial.component.ExternalAPICaller;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/")
public class ResilientAppController {
    private final ExternalAPICaller externalAPICaller;
    private final Logger logger = Logger.getLogger("ResilientAppController");

    @Autowired
    public ResilientAppController(ExternalAPICaller externalAPICaller) {
        this.externalAPICaller = externalAPICaller;
    }

    @GetMapping("/circuit-breaker")
    @CircuitBreaker(name = "CircuitBreakerService")
    public String circuitBreakerApi() {
        logger.log(Level.INFO, "called");
        return externalAPICaller.callApi();
    }

    @GetMapping("/retry")
    @Retry(name = "retryApi", fallbackMethod = "fallbackAfterRetry")
    public String retryApi() {
        return externalAPICaller.callApi();
    }

    public String fallbackAfterRetry(Exception e) {
        return "All Retries Have Exhausted";
    }

    @GetMapping("/time-limiter")
    @TimeLimiter(name = "timeLimiterApi")
    public CompletableFuture<String> timeLimiterApi() {
        return CompletableFuture.supplyAsync(externalAPICaller::callApiWithDelay);
    }

    @GetMapping("/bulkhead")
    @Bulkhead(name = "bulkheadApi")
    public String bulkheadApi() {
        return externalAPICaller.callApi();
    }
}
