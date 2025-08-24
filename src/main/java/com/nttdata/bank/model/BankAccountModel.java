package com.nttdata.bank.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class BankAccountModel {

    public Long accountId;

    public String accountNumber;

    public double balance = 0.0;

    public AccountType accountType;

    // AHORROS o CORRIENTE
    public enum AccountType {
        SAVINGS, CHECKING
    }

}
