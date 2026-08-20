package com.grits.paymentservice.kafka.event;

import com.grits.paymentservice.entity.status.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreatedEvent {

    private UUID paymentId;
    private UUID orderId;
    private PaymentStatus status;
}
