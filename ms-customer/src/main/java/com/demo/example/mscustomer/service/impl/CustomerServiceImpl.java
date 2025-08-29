package com.demo.example.mscustomer.service.impl;

import com.demo.example.mscustomer.dto.response.CustomerResponse;
import com.demo.example.mscustomer.infrastructure.mapper.CustomerMapper;
import com.demo.example.mscustomer.model.Customer;
import com.demo.example.mscustomer.repository.CustomerRepository;
import com.demo.example.mscustomer.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public List<CustomerResponse> getAllCustomer() {

        List<Customer> listCustomer = customerRepository.findAll();
        return CustomerMapper.toListResponse(listCustomer);
    }

    @Override
    public Optional<CustomerResponse> getCustomerById(int id) {

        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(CustomerMapper::convertCustomer);

    }
}
