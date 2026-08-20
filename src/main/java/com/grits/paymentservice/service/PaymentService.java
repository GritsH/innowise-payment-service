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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        String randomNumber = randomNumberClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new").trim();
        PaymentStatus status = Integer.parseInt(randomNumber) % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        Payment payment = paymentMapper.toEntity(request);
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus(status);
        Payment savedPayment = paymentDao.createPayment(payment);

        paymentKafkaProducer.sendPaymentCreatedEvent(savedPayment);
        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentDao.getPaymentsByOrderId(orderId);
        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        List<Payment> payments = paymentDao.getPaymentsByUserId(userId);
        return payments.stream().map(paymentMapper::toResponse).toList();
    }

    public List<PaymentResponse> getPaymentsByUserIdAndStatus(UUID userId, String status) {
        List<Payment> payments = paymentDao.getPaymentsByUserIdStatus(userId, status);
        return payments.stream().map(paymentMapper::toResponse).toList();
    }

    public BigDecimal getTotalAmountByUserIdAndDateRange(UUID userId, LocalDateTime from, LocalDateTime to) {
        return paymentDao.getTotalAmountByUserIdAndDateRange(userId, from, to);
    }

    public BigDecimal getTotalAmountByDateRange(LocalDateTime from, LocalDateTime to) {
        return paymentDao.getTotalAmountByDateRange(from, to);
    }
}
