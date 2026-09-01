package com.grits.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentStatusException extends GlobalServiceException {

    public InvalidPaymentStatusException(String status) {
        super("Invalid status: " + status, HttpStatus.BAD_REQUEST);
    }
}
