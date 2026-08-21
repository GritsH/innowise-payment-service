package com.grits.paymentservice.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AbstractIntegrationTest {

    static WireMockServer wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    static {
        mongo.start();
        wireMock.start();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "paymentservice");
        registry.add("api-gateway.url", () -> "http://localhost:" + wireMock.port());
        registry.add("random-number-client.url", () -> "http://localhost:" + wireMock.port());
    }
}
