package com.transaction_ms.transaction_ms.service;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.model.DepositoRequest;
import com.transaction_ms.transaction_ms.model.RetiroRequest;
import com.transaction_ms.transaction_ms.model.TransferenciaRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionCreator {
    public Transaction createTransactionDeposit(DepositoRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("DEPOSITO");
        tx.setMonto(request.getMonto());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        tx.setCuentaOrigenId(request.getCuentaId());
        return tx;
    }

    public Transaction createTransactionWithdrawal(RetiroRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("RETIRO");
        tx.setMonto(request.getMonto());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        tx.setCuentaOrigenId(request.getCuentaId());
        return tx;
    }

    public Transaction createTransactionTransfer(TransferenciaRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("TRANSFERENCIA");
        tx.setMonto(request.getMonto());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        tx.setCuentaOrigenId(request.getCuentaOrigenId());
        tx.setCuentaDestinoId(request.getCuentaDestinoId());
        return tx;
    }
}
