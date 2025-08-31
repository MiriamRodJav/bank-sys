package com.xyz.bank.customer_ms.controller;

import com.xyz.bank.customer_ms.model.CustomerModel;
import com.xyz.bank.customer_ms.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerModel> create(@RequestBody CustomerModel customer) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerModel>> findAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerModel> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerModel> update(@PathVariable Long id, @RequestBody CustomerModel customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

}
