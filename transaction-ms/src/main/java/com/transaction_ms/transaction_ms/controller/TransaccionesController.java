package com.transaction_ms.transaction_ms.controller;
import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
public class TransaccionesController implements TransaccionesApiDelegate {
    @Autowired
    private TransactionService transactionService;
    @Override
    public Mono<ResponseEntity<TransaccionResponse>> depositar(@RequestBody
            Mono<DepositoRequest> depositoRequest,
            ServerWebExchange exchange) {
        return depositoRequest
                .flatMap(request -> transactionService.processDeposit(request))
                .map(this::mapToResponse)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED))
                .onErrorResume(TransactionException.class, ex -> {
                    TransaccionResponse errorResponse = new TransaccionResponse();
                    errorResponse.setEstado(EstadoTransaccion.FAILED);
                    return Mono.just(new ResponseEntity<>(errorResponse, ex.getHttpStatus()));
                });
    }

    @Override
    public Mono<ResponseEntity<TransaccionResponse>> retirar(
            Mono<RetiroRequest> retiroRequest,
            ServerWebExchange exchange) {
        return retiroRequest
                .flatMap(request -> transactionService.processWithdrawal(request))
                .map(this::mapToResponse)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @Override
    public Mono<ResponseEntity<TransaccionResponse>> transferir(
            Mono<TransferenciaRequest> transferenciaRequest,
            ServerWebExchange exchange) {
        return transferenciaRequest
                .flatMap(request -> transactionService.processTransfer(request))
                .map(this::mapToResponse)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransaccionResponse>>> historial(String cuentaId, ServerWebExchange exchange) {

        Optional<String> optionalCuentaId = Optional.ofNullable(cuentaId)
                .filter(id -> !id.isBlank());

        Flux<TransaccionResponse> historialFlux = transactionService.findTransactionHistory(optionalCuentaId)
                .map(this::mapToResponse);

        return Mono.just(new ResponseEntity<>(historialFlux, HttpStatus.OK));
    }

    private TransaccionResponse mapToResponse(Transaction transaction) {
        TipoTransaccion tipo = (transaction.getTipo() != null) ? TipoTransaccion.fromValue(transaction.getTipo()) : null;
        EstadoTransaccion estado = (transaction.getEstado() != null) ? EstadoTransaccion.fromValue(transaction.getEstado()) : null;
        return new TransaccionResponse()
                .id(transaction.getId())
                .tipo(tipo)
                .estado(estado)
                .monto(transaction.getMonto())
                .fecha(transaction.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .cuentaOrigenId(transaction.getCuentaOrigenId())
                .cuentaDestinoId(transaction.getCuentaDestinoId())
                .referencia(transaction.getReferencia());
    }
}
