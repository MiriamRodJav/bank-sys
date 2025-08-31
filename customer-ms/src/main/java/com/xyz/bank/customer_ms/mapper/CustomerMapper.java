package com.xyz.bank.customer_ms.mapper;

import com.xyz.bank.customer_ms.entity.CustomerEntity;
import com.xyz.bank.customer_ms.model.CustomerModel;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerModel toModel(CustomerEntity entity) {
        if (entity == null) return null;
        return CustomerModel.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dni(entity.getDni())
                .email(entity.getEmail())
                .build();
    }

    public CustomerEntity toEntity(CustomerModel model) {
        if (model == null) return null;
        return CustomerEntity.builder()
                .id(model.getId())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .dni(model.getDni())
                .email(model.getEmail())
                .build();
    }

}
