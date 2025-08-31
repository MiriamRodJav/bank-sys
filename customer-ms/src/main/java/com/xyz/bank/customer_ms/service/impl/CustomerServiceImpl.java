package com.xyz.bank.customer_ms.service.impl;

import com.xyz.bank.customer_ms.entity.CustomerEntity;
import com.xyz.bank.customer_ms.mapper.CustomerMapper;
import com.xyz.bank.customer_ms.model.CustomerModel;
import com.xyz.bank.customer_ms.repository.CustomerRepository;
import com.xyz.bank.customer_ms.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public CustomerModel createCustomer(CustomerModel customer) {
        if (customerRepository.existsByDni(customer.getDni())) {
            throw new RuntimeException("Customer with DNI already exists.");
        }
        CustomerEntity entity = customerMapper.toEntity(customer);
        return customerMapper.toModel(customerRepository.save(entity));
    }

    @Override
    public List<CustomerModel> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerModel getCustomerById(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found."));
        return customerMapper.toModel(entity);
    }

    @Override
    public CustomerModel updateCustomer(Long id, CustomerModel customer) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found."));
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail());
        return customerMapper.toModel(customerRepository.save(entity));
    }

    @Override
    public void deleteCustomer(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found."));
        customerRepository.delete(entity);
    }

}
