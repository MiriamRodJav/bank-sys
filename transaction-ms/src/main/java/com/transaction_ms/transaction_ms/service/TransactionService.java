package com.transaction_ms.transaction_ms.service;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WebClient webClient;

    public TransactionService(TransactionRepository transactionRepository, @Value("${microservices.accounts.url:http://localhost:8081}") String accountsMicroserviceUrl) {
        this.transactionRepository = transactionRepository;
        this.webClient = WebClient.builder().baseUrl(accountsMicroserviceUrl).build();
    }


    public Mono<Transaction> processDeposit(DepositoRequest request) {

        Mono<Transaction> transactionMono = webClient.put()
                .uri("/cuentas/{cuentaId}/depositar", request.getCuentaId())
                .bodyValue(Map.of("monto", request.getMonto()))
                .retrieve()

                .bodyToMono(CuentaResponse.class)
                .map(resp -> createTransaction(request, "SUCCESS"))

                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.just(createTransaction(request, "FAILED"));
                })
                .onErrorResume(Exception.class, e -> {
                    return Mono.just(createTransaction(request, "FAILED"));
                });

        return transactionMono.flatMap(transactionRepository::save);
    }


    private Transaction createTransaction(DepositoRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("DEPOSITO");
        tx.setMonto(request.getMonto());
        tx.setCuentaDestinoId(request.getCuentaId());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        return tx;
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
