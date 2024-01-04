package com.example.resilience4jtutorial.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResilientAppControllerTest {
    @RegisterExtension
    static WireMockExtension EXTERNAL_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(9090)).build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCircuitBreaker() {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(serverError()));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    ResponseEntity<String> resp = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/external")));
    }

    @Test
    void testRetry() {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/retry", String.class);
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/api/external")));

        EXTERNAL_SERVICE.resetRequests();

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(serverError()));
        ResponseEntity<String> resp2 = restTemplate.getForEntity("/api/retry", String.class);
        assertEquals(resp2.getBody(), "All Retries Have Exhausted");
        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/api/external")));
    }

    @Test
    void testTimeLimiter() {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/time-limiter", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/api/external")));
    }

    /**
     * BulkheadFullException 어디 갔는데..? 추후에 버전 조사해서 체크
     * */
    @Test
    void testBulkhead() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        Map<Integer, Integer> respStatusCnt = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        IntStream.rangeClosed(1, 5).forEach(
                i -> executorService.execute(() -> {
                    System.out.println(i);
                    ResponseEntity<String> resp = restTemplate.getForEntity("/api/bulkhead", String.class);
                    int statueCode = resp.getStatusCode().value();
                    respStatusCnt.merge(statueCode, 1, Integer::sum);
                    latch.countDown();
                })
        );
        latch.await();
        executorService.shutdown();
        System.out.println("respStatusCnt.keySet() = " + respStatusCnt.keySet());
//        assertEquals(2, respStatusCnt.keySet().size());
        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/api/external")));
    }
}