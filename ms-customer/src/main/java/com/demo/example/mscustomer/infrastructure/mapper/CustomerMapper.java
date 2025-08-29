package com.demo.example.mscustomer.infrastructure.mapper;

import com.demo.example.mscustomer.dto.response.CustomerResponse;
import com.demo.example.mscustomer.infrastructure.util.MethodsUtil;
import com.demo.example.mscustomer.model.Customer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {

    public static List<CustomerResponse> toListResponse(List<Customer> listCustomer) {

        return MethodsUtil.mapSafe(listCustomer, CustomerMapper::convertCustomer);
    }

    public static CustomerResponse convertCustomer(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getIdCustomer())
                .name(customer.getFirstName())
                .dni(customer.getDni())
                .email(customer.getEmail())
                .build();
    }
}
