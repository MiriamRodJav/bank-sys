package com.xyz.bank.customer_ms.service;

import com.xyz.bank.customer_ms.model.CustomerModel;

import java.util.List;

public interface CustomerService {

    CustomerModel createCustomer(CustomerModel customer);

    List<CustomerModel> getAllCustomers();

    CustomerModel getCustomerById(Long id);

    CustomerModel updateCustomer(Long id, CustomerModel customer);

    void deleteCustomer(Long id);

}
