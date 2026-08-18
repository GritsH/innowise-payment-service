package com.grits.paymentservice.model.response;

import com.grits.paymentservice.entity.status.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;

    private UUID orderId;

    private UUID userId;

    private PaymentStatus status;

    private LocalDateTime timestamp;

    private BigDecimal paymentAmount;
}
