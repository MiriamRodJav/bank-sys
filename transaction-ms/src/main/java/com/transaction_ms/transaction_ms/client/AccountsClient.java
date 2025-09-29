package com.transaction_ms.transaction_ms.client;

import com.transaction_ms.transaction_ms.model.DepositoRequest;
import com.transaction_ms.transaction_ms.model.RetiroRequest;

public interface AccountsClient {

    void deposit(DepositoRequest request);
    void withdraw(RetiroRequest request);


}
