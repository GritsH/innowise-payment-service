package com.grits.paymentservice.client;

import com.grits.paymentservice.config.FeignAuthConfiguration;
import com.grits.paymentservice.model.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "api-gateway",
        url = "${api-gateway.url}",
        configuration = FeignAuthConfiguration.class
)
public interface ApiClient {

    @GetMapping("/api/users/email")
    UserResponse getUserByEmail(@RequestParam String email);
}
