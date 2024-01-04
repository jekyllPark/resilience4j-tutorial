package com.example.resilience4jtutorial.controller;

import com.example.resilience4jtutorial.component.ExternalAPICaller;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
