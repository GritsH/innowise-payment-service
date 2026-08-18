package com.grits.paymentservice.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class PaymentNotFoundException extends GlobalServiceException {

    public PaymentNotFoundException(UUID id) {
        super("Payment for id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
