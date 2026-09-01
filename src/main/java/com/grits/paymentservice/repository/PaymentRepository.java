package com.grits.paymentservice.repository;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    Page<Payment> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Payment> findByOrderId(UUID orderId);

    Page<Payment> findAllByUserIdAndStatus(UUID userId, PaymentStatus status, Pageable pageable);

    @Query("{ 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<Payment> findPaymentsByUserIdAndDateRange(UUID userId, Instant from, Instant to, Pageable pageable);

    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    Page<Payment> findPaymentsByDateRange(Instant from, Instant to, Pageable pageable);
}
