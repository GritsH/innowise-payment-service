package com.grits.paymentservice.repository;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    List<Payment> findAllByUserId(UUID userId);

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findAllByUserIdAndStatus(UUID userId, PaymentStatus status);

    @Aggregation(pipeline = {
            "{ '$match': { 'user_id': ?0, 'timestamp': { '$gte': ?1, '$lte': ?2 } } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$payment_amount' } } }"
    })
    Optional<TotalAmountResult> getTotalAmountByUserIdAndDateRange(UUID userId, LocalDateTime from, LocalDateTime to);

    @Aggregation(pipeline = {
            "{ '$match': { 'timestamp': { '$gte': ?0, '$lte': ?1 } } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$payment_amount' } } }"
    })
    Optional<TotalAmountResult> getTotalAmountByDateRange(LocalDateTime from, LocalDateTime to);
}
