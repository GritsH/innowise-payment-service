package com.grits.paymentservice.dao;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.exception.InvalidPaymentStatusException;
import com.grits.paymentservice.exception.PaymentNotFoundException;
import com.grits.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentDao {

    private final PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public UUID getUserIdByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException(orderId)).getUserId();
    }

    public Page<Payment> getPaymentsByUserId(UUID userId) {
        Pageable pageable = PageRequest.of(0, 10);
        return paymentRepository.findAllByUserId(userId, pageable);
    }

    public Payment getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException(orderId));
    }

    public Page<Payment> getPaymentsByUserIdStatus(UUID userId, String status) {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentStatusException(status);
        }
        Pageable pageable = PageRequest.of(0, 10);
        return paymentRepository.findAllByUserIdAndStatus(userId, paymentStatus, pageable);
    }

    public BigDecimal getTotalAmountByUserIdAndDateRange(UUID userId, Instant from, Instant to) {
        return paymentRepository.findPaymentsByUserIdAndDateRange(userId, from, to, Pageable.unpaged()).stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmountByDateRange(Instant from, Instant to) {
        return paymentRepository.findPaymentsByDateRange(from, to, Pageable.unpaged()).stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
