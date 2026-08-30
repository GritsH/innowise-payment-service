package com.grits.paymentservice.repository;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    List<Payment> findAllByUserId(UUID userId);

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findAllByUserIdAndStatus(UUID userId, PaymentStatus status);

    @Query("{ 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Payment> findPaymentsByUserIdAndDateRange(UUID userId, Instant from, Instant to);

    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Payment> findPaymentsByDateRange(Instant from, Instant to);
}
