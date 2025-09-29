package com.transaction_ms.transaction_ms.controller;


import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.mapper.TransactionMapper;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
public class TransaccionesController implements TransaccionesApiDelegate {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    @PostMapping("/transacciones/deposito")
    public Mono<ResponseEntity<TransaccionResponse>> depositar(
            Mono<DepositoRequest> depositoRequest,
            ServerWebExchange exchange) {

        return depositoRequest
                .flatMap(request -> transactionService.processDeposit(request))
                .map(transaction -> transactionMapper.toResponse(transaction))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    @PostMapping("/transacciones/retiro")
    public Mono<ResponseEntity<TransaccionResponse>> retirar(
            Mono<RetiroRequest> retiroRequest,
            ServerWebExchange exchange) {

        return retiroRequest
                .flatMap(request -> transactionService.processWithdraw(request))
                .map(transaction -> transactionMapper.toResponse(transaction))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    @PostMapping("/transacciones/transferencia")
    public Mono<ResponseEntity<TransaccionResponse>> transferir(
            Mono<TransferenciaRequest> transferenciaRequest,
            ServerWebExchange exchange) {

        return transferenciaRequest
                .flatMap(request -> transactionService.processTransfer(request))
                .map(transaction -> transactionMapper.toResponse(transaction))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    @GetMapping("/transacciones/historial")
    public Mono<ResponseEntity<Flux<TransaccionResponse>>> historial(
            EstadoTransaccion estado,
            TipoTransaccion tipo,
            ServerWebExchange exchange) {

        String estadoStr = (estado != null) ? estado.name() : null;
        String tipoStr = (tipo != null) ? tipo.name() : null;

        Flux<Transaction> domainFlux =
                transactionService.getTransactionHistory( estadoStr, tipoStr);

        Flux<TransaccionResponse> responseFlux =
                domainFlux.map(transactionMapper::toResponse);

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.OK)
                        .body(responseFlux)
        );
    }
}
