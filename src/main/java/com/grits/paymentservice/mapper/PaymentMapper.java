package com.grits.paymentservice.mapper;

import com.grits.paymentservice.entity.Payment;
import com.grits.paymentservice.model.request.CreatePaymentRequest;
import com.grits.paymentservice.model.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Payment toEntity(CreatePaymentRequest request);
}
