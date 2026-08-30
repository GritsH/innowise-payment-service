package com.grits.paymentservice.controller;

import com.grits.paymentservice.model.request.CreatePaymentRequest;
import com.grits.paymentservice.model.response.PaymentResponse;
import com.grits.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable UUID userId) {
        List<PaymentResponse> response = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserIdAndStatus(@PathVariable UUID userId, @PathVariable String status) {
        List<PaymentResponse> response = paymentService.getPaymentsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<BigDecimal> getTotalAmountByUserIdAndDateRange(@PathVariable UUID userId, @RequestParam Instant from, @RequestParam Instant to) {
        BigDecimal total = paymentService.getTotalAmountByUserIdAndDateRange(userId, from, to);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalAmountByDateRange(@RequestParam Instant from, @RequestParam Instant to) {
        BigDecimal total = paymentService.getTotalAmountByDateRange(from, to);
        return ResponseEntity.ok(total);
    }
}
