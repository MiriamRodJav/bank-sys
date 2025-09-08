package com.transaction_ms.transaction_ms.service;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.core.query.Query;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WebClient webClient;
    private final ReactiveMongoTemplate mongoTemplate;
    public TransactionService(TransactionRepository transactionRepository,
                              @Value("${microservices.accounts.url:http://localhost:8081}") String accountsMicroserviceUrl,
                              ReactiveMongoTemplate mongoTemplate) {
        this.transactionRepository = transactionRepository;
        this.webClient = WebClient.builder().baseUrl(accountsMicroserviceUrl).build();
        this.mongoTemplate = mongoTemplate;
    }


    public Mono<Transaction> processDeposit(DepositoRequest request) {

        return webClient.put()
                .uri("/cuentas/{cuentaId}/depositar", request.getCuentaId())
                .bodyValue(Map.of("monto", request.getMonto()))
                .retrieve()
                .bodyToMono(Void.class)
                .then(Mono.just(createTransactionDeposit(request, "SUCCESS")))
                .flatMap(transactionRepository::save)
                .onErrorResume(e -> {
                    Mono<Transaction> failedTransactionMono = Mono.just(createTransactionDeposit(request, "FAILED"));
                    return failedTransactionMono.flatMap(transactionRepository::save);
                });
    }

    private Transaction createTransactionDeposit(DepositoRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("DEPOSITO");
        tx.setMonto(request.getMonto());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        tx.setCuentaOrigenId(request.getCuentaId());
        return tx;
    }

    public Mono<Transaction> processWithdrawal(RetiroRequest request) {

        return webClient.put()
                .uri("/cuentas/{cuentaId}/retirar", request.getCuentaId())
                .bodyValue(Map.of("monto", request.getMonto()))
                .retrieve()
                .bodyToMono(Void.class)
                .then(Mono.just(createTransactionWithdrawal(request, "SUCCESS")))
                .flatMap(transactionRepository::save)
                .onErrorResume(e -> {
                    Mono<Transaction> failedTransactionMono = Mono.just(createTransactionWithdrawal(request, "FAILED"));
                    return failedTransactionMono.flatMap(transactionRepository::save);
                });

    }
    private Transaction createTransactionWithdrawal(RetiroRequest request, String status) {
        Transaction tx = new Transaction();
        tx.setTipo("RETIRO");
        tx.setMonto(request.getMonto());
        tx.setFecha(LocalDateTime.now());
        tx.setEstado(status);
        tx.setReferencia(request.getReferencia());
        tx.setCuentaOrigenId(request.getCuentaId());
        return tx;
    }
    public Mono<Transaction> processTransfer(TransferenciaRequest request) {
        return webClient.put()
                .uri("/cuentas/{cuentaId}/retirar", request.getCuentaOrigenId())
                .bodyValue(Map.of("monto", request.getMonto()))
                .retrieve()
                .bodyToMono(Void.class)

                .then(
                        webClient.put()
                                .uri("/cuentas/{cuentaId}/depositar", request.getCuentaDestinoId())
                                .bodyValue(Map.of("monto", request.getMonto()))
                                .retrieve()
                                .bodyToMono(Void.class)
                )

                .then(Mono.defer(() -> {
                    Transaction tx = createTransactionTransfer(request, "SUCCESS");
                    return transactionRepository.save(tx);
                }))

                .onErrorResume(e -> {
                    Transaction tx = createTransactionTransfer(request, "FAILED");
                    return transactionRepository.save(tx);
                });
    }
    private Transaction createTransactionTransfer(TransferenciaRequest request, String status) {
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
    public Flux<Transaction> getFilteredHistory(String accountId, String estado, String tipo) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(accountId)) {
            Criteria accountCriteria = new Criteria().orOperator(
                    Criteria.where("cuentaOrigenId").is(accountId),
                    Criteria.where("cuentaDestinoId").is(accountId)
            );
            criteria.andOperator(accountCriteria);
        }

        if (StringUtils.hasText(estado)) {
            criteria.and("estado").is(estado);
        }

        if (StringUtils.hasText(tipo)) {
            criteria.and("tipo").is(tipo);
        }

        query.addCriteria(criteria);

        return mongoTemplate.find(query, Transaction.class);
    }
}
