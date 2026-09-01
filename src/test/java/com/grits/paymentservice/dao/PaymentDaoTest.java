package com.grits.paymentservice.dao;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import com.grits.paymentservice.exception.InvalidPaymentStatusException;
import com.grits.paymentservice.exception.PaymentNotFoundException;
import com.grits.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentDaoTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentDao paymentDao;

    private UUID orderId;
    private UUID userId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        UUID paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(Instant.now());
        payment.setPaymentAmount(new BigDecimal("999.99"));
    }

    @Test
    @DisplayName("should create payment")
    void createPayment() {
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = paymentDao.createPayment(payment);

        assertThat(result).isEqualTo(payment);

        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("should get user id by order id")
    void getUserIdByOrderId() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        UUID result = paymentDao.getUserIdByOrderId(orderId);

        assertThat(result).isEqualTo(userId);

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("should throw exception when getting user id by nonexistent order id")
    void throwExceptionWhenGettingUserByNonexistentOrderId() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentDao.getUserIdByOrderId(orderId)).isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("should get payments by user id")
    void getPaymentsByUserId() {
        Page<Payment> payments = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(payments);

        Page<Payment> result = paymentDao.getPaymentsByUserId(userId);

        assertThat(result).isNotNull().containsExactly(payment);

        verify(paymentRepository).findAllByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty list when user has no payments")
    void returnEmptyListWhenUserHasNoPayments() {
        Page<Payment> payments = new PageImpl<>(List.of());

        when(paymentRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(payments);

        Page<Payment> result = paymentDao.getPaymentsByUserId(userId);

        assertThat(result).isEmpty();

        verify(paymentRepository).findAllByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("should get payment by order id")
    void getPaymentByOrderId() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        Payment result = paymentDao.getPaymentsByOrderId(orderId);

        assertThat(result).isEqualTo(payment);

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("should get payments by user id and status")
    void getPaymentsByUserIdAndStatus() {
        String status = "SUCCESS";
        Page<Payment> payments = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAllByUserIdAndStatus(eq(userId), eq(PaymentStatus.SUCCESS), any(Pageable.class))).thenReturn(payments);

        Page<Payment> result = paymentDao.getPaymentsByUserIdStatus(userId, status);

        assertThat(result).isNotNull().containsExactly(payment);

        verify(paymentRepository).findAllByUserIdAndStatus(eq(userId), eq(PaymentStatus.SUCCESS), any(Pageable.class));
    }

    @Test
    @DisplayName("should accept lowercase payment status")
    void acceptLowercasePaymentStatus() {
        String status = "success";
        Page<Payment> payments = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAllByUserIdAndStatus(eq(userId), eq(PaymentStatus.SUCCESS), any(Pageable.class))).thenReturn(payments);

        Page<Payment> result = paymentDao.getPaymentsByUserIdStatus(userId, status);

        assertThat(result).containsExactly(payment);

        verify(paymentRepository).findAllByUserIdAndStatus(eq(userId), eq(PaymentStatus.SUCCESS), any(Pageable.class));
    }

    @Test
    @DisplayName("should throw invalid payment status exception")
    void throwInvalidPaymentStatusException() {
        String invalidStatus = "INVALID_STATUS";

        assertThatThrownBy(() -> paymentDao.getPaymentsByUserIdStatus(userId, invalidStatus)).isInstanceOf(InvalidPaymentStatusException.class);

        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("should get total amount by user id and date range")
    void getTotalAmountByUserIdAndDateRange() {
        Instant from = Instant.parse("2026-08-21T10:00:00Z");
        Instant to = Instant.parse("2026-08-21T18:00:00Z");
        Payment payment1 = new Payment();
        payment1.setPaymentAmount(new BigDecimal("100.00"));
        Payment payment2 = new Payment();
        payment2.setPaymentAmount(new BigDecimal("200.00"));
        Page<Payment> payments = new PageImpl<>(List.of(payment1, payment2));

        when(paymentRepository.findPaymentsByUserIdAndDateRange(eq(userId), eq(from), eq(to), eq(Pageable.unpaged()))).thenReturn(payments);

        BigDecimal result = paymentDao.getTotalAmountByUserIdAndDateRange(userId, from, to);

        assertThat(result).isEqualByComparingTo(new BigDecimal("300.00"));

        verify(paymentRepository).findPaymentsByUserIdAndDateRange(eq(userId), eq(from), eq(to), eq(Pageable.unpaged()));
    }

    @Test
    @DisplayName("should get total amount by date range")
    void getTotalAmountByDateRange() {
        Instant from = Instant.parse("2026-08-21T10:00:00Z");
        Instant to = Instant.parse("2026-08-21T18:00:00Z");
        Payment payment1 = new Payment();
        payment1.setPaymentAmount(new BigDecimal("100.00"));
        Payment payment2 = new Payment();
        payment2.setPaymentAmount(new BigDecimal("200.00"));
        Page<Payment> payments = new PageImpl<>(List.of(payment1, payment2));

        when(paymentRepository.findPaymentsByDateRange(eq(from), eq(to), eq(Pageable.unpaged()))).thenReturn(payments);

        BigDecimal result = paymentDao.getTotalAmountByDateRange(from, to);

        assertThat(result).isEqualByComparingTo(new BigDecimal("300.00"));

        verify(paymentRepository).findPaymentsByDateRange(eq(from), eq(to), eq(Pageable.unpaged()));
    }
}