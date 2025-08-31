package com.xyz.bank.customer_ms.feing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-ms", url = "http://localhost:8081/accounts")
public interface AccountClient {

    @GetMapping("/existsByCustomer/{customerId}")
    Boolean existsByCustomer(@PathVariable("customerId") Long customerId);

}