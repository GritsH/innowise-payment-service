package com.grits.paymentservice.dao;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentDao {

    private final PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    public BigDecimal getTotalAmountByUserIdAndDateRange(UUID userId, LocalDateTime from, LocalDateTime to) {
        return paymentRepository.getTotalAmountByUserIdAndDateRange(userId, from, to);
    }

    public BigDecimal getTotalAmountByDateRange(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.getTotalAmountByDateRange(from, to);
    }
}
