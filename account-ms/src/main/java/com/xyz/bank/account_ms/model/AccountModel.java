package com.xyz.bank.account_ms.model;

import com.xyz.bank.account_ms.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountModel {

    private Long id;

    private String accountNumber;

    private Double balance;

    private AccountType accountType;

    private Long customerId;

}