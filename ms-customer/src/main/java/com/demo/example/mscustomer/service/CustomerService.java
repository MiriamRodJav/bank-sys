package com.demo.example.mscustomer.service;

import com.demo.example.mscustomer.dto.response.CustomerResponse;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<CustomerResponse> getAllCustomer();
    Optional<CustomerResponse> getCustomerById(int id);
}
