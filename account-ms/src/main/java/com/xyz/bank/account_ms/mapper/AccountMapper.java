package com.xyz.bank.account_ms.mapper;

import com.xyz.bank.account_ms.entity.AccountEntity;
import com.xyz.bank.account_ms.model.AccountModel;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountModel toModel(AccountEntity entity) {
        if (entity == null) return null;
        return AccountModel.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .balance(entity.getBalance())
                .accountType(entity.getAccountType())
                .customerId(entity.getCustomerId())
                .build();
    }

    public AccountEntity toEntity(AccountModel model) {
        if (model == null) return null;
        return AccountEntity.builder()
                .id(model.getId())
                .accountNumber(model.getAccountNumber())
                .balance(model.getBalance())
                .accountType(model.getAccountType())
                .customerId(model.getCustomerId())
                .build();
    }

}