package com.grits.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.kafka.PaymentKafkaProducer;
import com.grits.paymentservice.model.request.CreatePaymentRequest;
import com.grits.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @MockitoBean
    PaymentKafkaProducer paymentKafkaProducer;

    private static final String USER_EMAIL = "john@gmail.com";

    private static final UUID USER_ID = UUID.randomUUID();

    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    @BeforeEach
    void clean() {
        paymentRepository.deleteAll();
        wireMock.resetAll();
    }

    @Test
    @DisplayName("should create payment")
    void createPayment() {
        try {
            stubRandomNumber("2");
            CreatePaymentRequest request = createPaymentRequest();

            mockMvc.perform(post("/v1/payments")
                            .with(userJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").value(request.getOrderId().toString()))
                    .andExpect(jsonPath("$.userId").value(request.getUserId().toString()))
                    .andExpect(jsonPath("$.paymentAmount").value(100.00))
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.timestamp").exists());

            assertThat(paymentRepository.count()).isEqualTo(1);

            Payment savedPayment = paymentRepository.findAll().getFirst();

            assertThat(savedPayment.getOrderId()).isEqualTo(request.getOrderId());
            assertThat(savedPayment.getUserId()).isEqualTo(request.getUserId());
            assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(request.getPaymentAmount());
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(savedPayment.getTimestamp()).isNotNull();

            wireMock.verify(1, getRequestedFor(urlPathEqualTo("/integers")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should create failed payment when random number is odd")
    void createFailedPayment() {
        try {
            stubRandomNumber("3");
            CreatePaymentRequest request = createPaymentRequest();

            mockMvc.perform(post("/v1/payments")
                            .with(userJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("FAILED"));

            Payment savedPayment = paymentRepository.findAll().getFirst();

            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return payment by order id")
    void returnPaymentByOrderId() {
        try {
            Instant timestamp = Instant.parse("2026-08-21T10:00:00Z");
            Payment payment = createPayment(USER_ID, PaymentStatus.SUCCESS, new BigDecimal("100.00"), timestamp);
            stubUserByEmail();

            mockMvc.perform(get("/v1/payments/order/{orderId}", payment.getOrderId())
                            .with(userJwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                    .andExpect(jsonPath("$.orderId").value(payment.getOrderId().toString()))
                    .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.timestamp").value(timestamp.toString()))
                    .andExpect(jsonPath("$.paymentAmount").value(100.00));

            Payment savedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

            assertThat(savedPayment.getId()).isEqualTo(payment.getId());
            assertThat(savedPayment.getOrderId()).isEqualTo(payment.getOrderId());
            assertThat(savedPayment.getUserId()).isEqualTo(USER_ID);
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(savedPayment.getTimestamp()).isEqualTo(timestamp);
            assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo("100.00");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return payments by user id")
    void returnPaymentsByUserId() {
        try {
            createPayment(USER_ID, PaymentStatus.SUCCESS, new BigDecimal("100.00"), Instant.now());
            createPayment(USER_ID, PaymentStatus.FAILED, new BigDecimal("50.00"), Instant.now());
            stubUserByEmail();

            mockMvc.perform(get("/v1/payments/user/{userId}", USER_ID)
                            .with(userJwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return 403 when user accesses another user's payment by order id")
    void return403WhenUserAccessesAnotherUsersPayment() {
        try {
            Payment payment = createPayment(OTHER_USER_ID, PaymentStatus.SUCCESS, new BigDecimal("100.00"), Instant.now());
            stubUserByEmail();

            mockMvc.perform(get("/v1/payments/order/{orderId}", payment.getOrderId())
                            .with(userJwt()))
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return total amount by user id and date range")
    void returnTotalAmountByUserIdAndDateRange() {
        try {
            Instant baseTime = Instant.parse("2026-08-21T10:00:00Z");
            Instant from = baseTime.minus(3, ChronoUnit.HOURS);
            Instant to = baseTime.plus(3, ChronoUnit.HOURS);
            createPayment(USER_ID, PaymentStatus.SUCCESS, new BigDecimal("100.00"), baseTime.minus(2, ChronoUnit.HOURS));
            createPayment(USER_ID, PaymentStatus.SUCCESS, new BigDecimal("200.00"), baseTime.minus(1, ChronoUnit.HOURS));

            stubUserByEmail();

            mockMvc.perform(get("/v1/payments/user/{userId}/total", USER_ID)
                            .param("from", from.toString())
                            .param("to", to.toString())
                            .with(userJwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("300.00"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return total amount by date range for admin")
    void returnTotalAmountByDateRangeForAdmin() {
        try {
            Instant baseTime = Instant.parse("2026-08-21T10:00:00Z");
            Instant from = baseTime.minus(3, ChronoUnit.HOURS);
            Instant to = baseTime.plus(3, ChronoUnit.HOURS);
            createPayment(OTHER_USER_ID, PaymentStatus.SUCCESS, new BigDecimal("50.00"), baseTime.minus(1, ChronoUnit.HOURS));
            createPayment(USER_ID, PaymentStatus.SUCCESS, new BigDecimal("100.00"), baseTime.minus(2, ChronoUnit.HOURS));
            createPayment(USER_ID, PaymentStatus.FAILED, new BigDecimal("200.00"), baseTime.plus(1, ChronoUnit.HOURS));

            stubUserByEmail();

            mockMvc.perform(get("/v1/payments/total")
                            .param("from", from.toString())
                            .param("to", to.toString())
                            .with(adminJwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("350.00"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should return 401 when user is not authenticated")
    void return401WhenUserIsNotAuthenticated() {
        try {
            mockMvc.perform(get("/v1/payments/user/{userId}", USER_ID))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreatePaymentRequest createPaymentRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();

        request.setOrderId(UUID.randomUUID());
        request.setUserId(USER_ID);
        request.setPaymentAmount(new BigDecimal("100.00"));

        return request;
    }

    private Payment createPayment(UUID userId, PaymentStatus status, BigDecimal amount, Instant timestamp) {
        Payment payment = new Payment();

        payment.setOrderId(UUID.randomUUID());
        payment.setUserId(userId);
        payment.setStatus(status);
        payment.setTimestamp(timestamp);
        payment.setPaymentAmount(amount);

        return paymentRepository.save(payment);
    }

    private void stubRandomNumber(String randomNumber) {
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/integers")).willReturn(ok(randomNumber)));
    }

    private void stubUserByEmail() {
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/users/email"))
                .withQueryParam("email", equalTo(USER_EMAIL))
                .willReturn(okJson("""
                        {
                          "id": "%s",
                          "name": "John",
                          "surname": "Doe",
                          "email": "%s"
                        }
                        """.formatted(
                        USER_ID,
                        USER_EMAIL
                )))
        );
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt().jwt(jwt -> jwt.claim("email", USER_EMAIL)).authorities();
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
        return jwt().jwt(jwt -> jwt.claim("email", USER_EMAIL)).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}