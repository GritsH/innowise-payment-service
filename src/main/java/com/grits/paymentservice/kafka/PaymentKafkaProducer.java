package com.grits.paymentservice.kafka;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.kafka.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.create-payment}")
    private String topic;

    public void sendPaymentCreatedEvent(Payment payment) {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .build();

        kafkaTemplate.send(topic, payment.getOrderId().toString(), event);
    }
}
