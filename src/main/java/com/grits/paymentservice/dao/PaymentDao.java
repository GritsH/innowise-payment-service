package com.grits.paymentservice.dao;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.exception.InvalidPaymentStatusException;
import com.grits.paymentservice.exception.PaymentNotFoundException;
import com.grits.paymentservice.repository.PaymentRepository;
import com.grits.paymentservice.repository.TotalAmountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    public List<Payment> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findAllByUserId(userId);
    }

    public Payment getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException(orderId));
    }

    public List<Payment> getPaymentsByUserIdStatus(UUID userId, String status) {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentStatusException(status);
        }
        return paymentRepository.findAllByUserIdAndStatus(userId, paymentStatus);
    }

    public BigDecimal getTotalAmountByUserIdAndDateRange(UUID userId, LocalDateTime from, LocalDateTime to) {
        return paymentRepository
                .getTotalAmountByUserIdAndDateRange(userId, from, to)
                .map(TotalAmountResult::getTotal)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalAmountByDateRange(LocalDateTime from, LocalDateTime to) {
        return paymentRepository
                .getTotalAmountByDateRange(from, to)
                .map(TotalAmountResult::getTotal)
                .orElse(BigDecimal.ZERO);
    }
}
