package com.transaction_ms.transaction_ms.service;

import com.transaction_ms.transaction_ms.client.AccountsClient;
import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.*;

import com.transaction_ms.transaction_ms.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import reactor.core.scheduler.Schedulers;


@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountsClient accountsClient;

    @Autowired
    private TransactionCreator transactionCreator;

    public Mono<Transaction> processDeposit(DepositoRequest request) {

        return Mono.fromCallable(() -> {

                    accountsClient.deposit(request);

                    Transaction newTransaction =
                            transactionCreator.createTransactionDeposit(request, "SUCCESS");

                    return newTransaction;
                })

                .subscribeOn(Schedulers.boundedElastic())

                .flatMap(transactionRepository::save)
                .onErrorResume(TransactionException.class, ex -> {

                    Transaction failedTransaction =
                            transactionCreator.createTransactionDeposit(request, "FAILED");

                    Mono<Transaction> saveFailedTransaction =
                            transactionRepository.save(failedTransaction);

                    return saveFailedTransaction
                            .then(Mono.error(ex));
                });
    }


    public Mono<Transaction> processWithdraw(RetiroRequest request) {

        return Mono.fromCallable(() -> {
                    accountsClient.withdraw(request);
                    Transaction newTransaction =
                            transactionCreator.createTransactionWithdrawal(request, "SUCCESS");
                    return newTransaction;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(transactionRepository::save)
                .onErrorResume(TransactionException.class, ex -> {
                    Transaction failedTransaction =
                            transactionCreator.createTransactionWithdrawal(request, "FAILED");
                    Mono<Transaction> saveFailedTransaction =
                            transactionRepository.save(failedTransaction);
                    return saveFailedTransaction
                            .then(Mono.error(ex));
                });
    }
    public Mono<Transaction> processTransfer(TransferenciaRequest request) {

        return Mono.fromCallable(() -> {

                    try {
                        accountsClient.withdraw(new RetiroRequest(
                                request.getCuentaOrigenId(),
                                request.getMonto()
                        ));
                    } catch (TransactionException ex) {
                        throw ex;
                    }

                    try {
                        accountsClient.deposit(new DepositoRequest(
                                request.getCuentaDestinoId(),
                                request.getMonto()
                        ));
                    } catch (TransactionException ex) {
                        try {
                            accountsClient.deposit(new DepositoRequest(
                                    request.getCuentaOrigenId(),
                                    request.getMonto()
                            ));
                        } catch (Exception compensationEx) {
                            throw ex;
                        }

                        throw ex;
                    }

                    Transaction newTransaction =
                            transactionCreator.createTransactionTransfer(request, "SUCCESS");
                    return newTransaction;

                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(transactionRepository::save)
                .onErrorResume(TransactionException.class, ex -> {

                    Transaction failedTransaction =
                            transactionCreator.createTransactionTransfer(request, "FAILED");

                    return transactionRepository.save(failedTransaction)
                            .then(Mono.error(ex));
                });
    }


    public Flux<Transaction> getTransactionHistory(String estado, String tipo) {

        Flux<Transaction> transactions =
                transactionRepository.findAll();

        return transactions
                .filter(t -> estado == null || estado.equalsIgnoreCase(t.getEstado()))
                .filter(t -> tipo == null || tipo.equalsIgnoreCase(t.getTipo()));

    }

}
