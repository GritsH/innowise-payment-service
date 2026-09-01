package com.grits.paymentservice.service;

import com.grits.paymentservice.client.RandomNumberClient;
import com.grits.paymentservice.dao.PaymentDao;
import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.kafka.PaymentKafkaProducer;
import com.grits.paymentservice.mapper.PaymentMapper;
import com.grits.paymentservice.model.request.CreatePaymentRequest;
import com.grits.paymentservice.model.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RandomNumberClient randomNumberClient;

    private final PaymentDao paymentDao;

    private final PaymentMapper paymentMapper;

    private final PaymentKafkaProducer paymentKafkaProducer;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);

        String randomNumber = randomNumberClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new").trim();
        boolean isPaymentSuccessful = Integer.parseInt(randomNumber) % 2 == 0;
        PaymentStatus status = isPaymentSuccessful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        payment.setTimestamp(Instant.now());
        payment.setStatus(status);
        Payment savedPayment = paymentDao.createPayment(payment);

        paymentKafkaProducer.sendPaymentCreatedEvent(savedPayment);
        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentDao.getPaymentsByOrderId(orderId);
        return paymentMapper.toResponse(payment);
    }

    public Page<PaymentResponse> getPaymentsByUserId(UUID userId) {
        Page<Payment> payments = paymentDao.getPaymentsByUserId(userId);
        List<PaymentResponse> responses = payments.stream().map(paymentMapper::toResponse).toList();
        return new PageImpl<>(responses, payments.getPageable(), payments.getTotalElements());
    }

    public Page<PaymentResponse> getPaymentsByUserIdAndStatus(UUID userId, String status) {
        Page<Payment> payments = paymentDao.getPaymentsByUserIdStatus(userId, status);
        List<PaymentResponse> responses = payments.stream().map(paymentMapper::toResponse).toList();
        return new PageImpl<>(responses, payments.getPageable(), payments.getTotalElements());
    }

    public BigDecimal getTotalAmountByUserIdAndDateRange(UUID userId, Instant from, Instant to) {
        return paymentDao.getTotalAmountByUserIdAndDateRange(userId, from, to);
    }

    public BigDecimal getTotalAmountByDateRange(Instant from, Instant to) {
        return paymentDao.getTotalAmountByDateRange(from, to);
    }
}
