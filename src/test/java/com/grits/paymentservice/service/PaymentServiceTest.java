package com.grits.paymentservice.service;

import com.grits.paymentservice.client.RandomNumberClient;
import com.grits.paymentservice.dao.PaymentDao;
import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.kafka.PaymentKafkaProducer;
import com.grits.paymentservice.mapper.PaymentMapper;
import com.grits.paymentservice.model.request.CreatePaymentRequest;
import com.grits.paymentservice.model.response.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private RandomNumberClient randomNumberClient;

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentKafkaProducer paymentKafkaProducer;

    @InjectMocks
    private PaymentService paymentService;

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;

    private Payment payment;
    private PaymentResponse paymentResponse;
    private CreatePaymentRequest request;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setPaymentAmount(new BigDecimal("999.99"));

        payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setPaymentAmount(new BigDecimal("999.99"));

        paymentResponse = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("999.99"))
                .build();
    }

    @Test
    @DisplayName("should create payment with success status when random number is even")
    void createPaymentWithSuccessStatus() {
        when(randomNumberClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new")).thenReturn("2");
        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(paymentDao.createPayment(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(paymentResponse);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getTimestamp()).isNotNull();

        verify(randomNumberClient).getRandomNumber(1, 1, 100, 1, 10, "plain", "new");
        verify(paymentMapper).toEntity(request);
        verify(paymentDao).createPayment(payment);
        verify(paymentKafkaProducer).sendPaymentCreatedEvent(payment);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("should create payment with failed status when random number is odd")
    void createPaymentWithFailedStatus() {
        PaymentResponse failedResponse = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .status(PaymentStatus.FAILED)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("999.99"))
                .build();

        when(randomNumberClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new")).thenReturn("3");
        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(paymentDao.createPayment(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(failedResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(failedResponse);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getTimestamp()).isNotNull();

        verify(randomNumberClient).getRandomNumber(1, 1, 100, 1, 10, "plain", "new");
        verify(paymentMapper).toEntity(request);
        verify(paymentDao).createPayment(payment);
        verify(paymentKafkaProducer).sendPaymentCreatedEvent(payment);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("should return payment by order id")
    void returnPaymentByOrderId() {
        when(paymentDao.getPaymentsByOrderId(orderId)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.getPaymentByOrderId(orderId);

        assertThat(result).isEqualTo(paymentResponse);

        verify(paymentDao).getPaymentsByOrderId(orderId);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("should get payments by user id")
    void getPaymentsByUserId() {
        Payment secondPayment = new Payment();
        secondPayment.setId(UUID.randomUUID());
        secondPayment.setUserId(userId);
        PaymentResponse secondResponse = PaymentResponse.builder()
                .id(secondPayment.getId())
                .userId(userId)
                .status(PaymentStatus.FAILED)
                .build();
        List<Payment> payments = List.of(payment, secondPayment);

        when(paymentDao.getPaymentsByUserId(userId)).thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);
        when(paymentMapper.toResponse(secondPayment)).thenReturn(secondResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByUserId(userId);

        assertThat(result).containsExactly(paymentResponse, secondResponse);

        verify(paymentDao).getPaymentsByUserId(userId);
        verify(paymentMapper).toResponse(payment);
        verify(paymentMapper).toResponse(secondPayment);
    }

    @Test
    @DisplayName("should get payments by user id and status")
    void getPaymentsByUserIdAndStatus() {
        String status = "SUCCESS";

        when(paymentDao.getPaymentsByUserIdStatus(userId, status)).thenReturn(List.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByUserIdAndStatus(userId, status);

        assertThat(result).containsExactly(paymentResponse);

        verify(paymentDao).getPaymentsByUserIdStatus(userId, status);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("should get total amount by user id and date range")
    void getTotalAmountByUserIdAndDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        BigDecimal total = new BigDecimal("999.99");

        when(paymentDao.getTotalAmountByUserIdAndDateRange(userId, from, to)).thenReturn(total);

        BigDecimal result = paymentService.getTotalAmountByUserIdAndDateRange(userId, from, to);

        assertThat(result).isEqualByComparingTo(total);

        verify(paymentDao).getTotalAmountByUserIdAndDateRange(userId, from, to);
    }

    @Test
    @DisplayName("should get total amount by date range")
    void getTotalAmountByDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        BigDecimal total = new BigDecimal("999.99");

        when(paymentDao.getTotalAmountByDateRange(from, to)).thenReturn(total);

        BigDecimal result = paymentService.getTotalAmountByDateRange(from, to);

        assertThat(result).isEqualByComparingTo(total);

        verify(paymentDao).getTotalAmountByDateRange(from, to);
    }
}