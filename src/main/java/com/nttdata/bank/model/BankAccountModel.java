package com.nttdata.bank.model;

import lombok.*;

/**
 * Represents a bank account that belongs to a client.
 * Each account has an identifier, number, balance, and type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class BankAccountModel {

    /** Unique identifier of the account. */
    private Long accountId;

    /** Account number used for operations. */
    private String accountNumber;

    /** Current balance of the account. */
    private double balance = 0.0;

    /** Type of account: savings or checking. */
    private AccountType accountType;

    /** Unique identifier of the client. */
    private Long clientId;

    /**
     * Enumeration of possible account types.
     * SAVINGS: Savings account/Cuenta de ahorros
     * CHECKING: Checking account/Cuenta corriente.
     */
    public enum AccountType {
        SAVINGS, CHECKING
    }
}
