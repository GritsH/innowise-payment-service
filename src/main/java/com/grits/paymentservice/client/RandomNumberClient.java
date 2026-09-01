package com.grits.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "random-number-client",
        url = "${random-number-client.url}"
)
public interface RandomNumberClient {
    @GetMapping("/integers")
    String getRandomNumber(
            @RequestParam("num") int num,
            @RequestParam("min") int min,
            @RequestParam("max") int max,
            @RequestParam("col") int col,
            @RequestParam("base") int base,
            @RequestParam("format") String format,
            @RequestParam("rnd") String rnd
    );
}
