package com.grits.paymentservice.repository;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.entity.status.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByUserId(UUID userId);

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findAllByUserIdAndStatus(UUID userId, PaymentStatus status);

    @Query("""
            select coalesce(SUM(p.paymentAmount), 0) 
            from Payment p
            where p.userId = :userId
              and p.timestamp between :from and :to
            """)
    BigDecimal getTotalAmountByUserIdAndDateRange(@Param("userId") UUID userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select coalesce(SUM(p.paymentAmount), 0) 
            from Payment p
            where p.timestamp between :from and :to
            """)
    BigDecimal getTotalAmountByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
