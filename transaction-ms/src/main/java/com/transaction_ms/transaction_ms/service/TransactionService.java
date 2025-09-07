package com.transaction_ms.transaction_ms.service;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public Mono<Transaction> processDeposit(DepositoRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTipo("DEPOSITO");
        transaction.setMonto(request.getMonto());
        transaction.setCuentaDestinoId(request.getCuentaId());
        transaction.setFecha(LocalDateTime.now());
        transaction.setEstado("SUCCESS");
        transaction.setReferencia(request.getReferencia());
        return transactionRepository.save(transaction);
    }

    public Mono<Transaction> processWithdrawal(RetiroRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTipo("RETIRO");
        transaction.setMonto(request.getMonto());
        transaction.setCuentaOrigenId(request.getCuentaId());
        transaction.setFecha(LocalDateTime.now());
        transaction.setEstado("SUCCESS");
        transaction.setReferencia(request.getReferencia());
        return transactionRepository.save(transaction);
    }

    public Mono<Transaction> processTransfer(TransferenciaRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTipo("TRANSFERENCIA");
        transaction.setMonto(request.getMonto());
        transaction.setCuentaOrigenId(request.getCuentaOrigenId());
        transaction.setCuentaDestinoId(request.getCuentaDestinoId());
        transaction.setFecha(LocalDateTime.now());
        transaction.setEstado("SUCCESS");
        transaction.setReferencia(request.getReferencia());
        return transactionRepository.save(transaction);
    }

    public Flux<Transaction> findTransactionHistory(Optional<String> cuentaId) {
        if (cuentaId.isPresent()) {
            return transactionRepository.findByCuentaOrigenId(cuentaId.get());
        } else {
            return transactionRepository.findAll();
        }
    }
}
