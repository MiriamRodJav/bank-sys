package com.transaction_ms.transaction_ms.api;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class TransaccionesApiDelegateImpl implements TransaccionesApiDelegate {

    private final TransactionService transactionService;

    public TransaccionesApiDelegateImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // POST /transacciones/deposito
    @Override
    public Mono<ResponseEntity<TransaccionResponse>> depositar(
            String idempotencyKey,
            Mono<DepositoRequest> depositoRequest,
            ServerWebExchange exchange) {

        // Si quieres, puedes usar idempotencyKey en tu service para evitar duplicados.
        return depositoRequest
                .flatMap(req -> transactionService.processDeposit(req))   // -> Mono<Transaction>
                .map(this::mapToResponse)                                 // -> TransaccionResponse
                .map(resp -> new ResponseEntity<>(resp, HttpStatus.CREATED));
    }

    // POST /transacciones/retiro
    @Override
    public Mono<ResponseEntity<TransaccionResponse>> retirar(
            String idempotencyKey,
            Mono<RetiroRequest> retiroRequest,
            ServerWebExchange exchange) {

        return retiroRequest
                .flatMap(req -> transactionService.processWithdrawal(req))
                .map(this::mapToResponse)
                .map(resp -> new ResponseEntity<>(resp, HttpStatus.CREATED));
    }

    // POST /transacciones/transferencia
    @Override
    public Mono<ResponseEntity<TransaccionResponse>> transferir(
            String idempotencyKey,
            Mono<TransferenciaRequest> transferenciaRequest,
            ServerWebExchange exchange) {

        return transferenciaRequest
                .flatMap(req -> transactionService.processTransfer(req))
                .map(this::mapToResponse)
                .map(resp -> new ResponseEntity<>(resp, HttpStatus.CREATED));
    }

    // GET /transacciones/historial
    @Override
    public Mono<ResponseEntity<Flux<TransaccionResponse>>> historial(
            String cuentaId,
            EstadoTransaccion estado,
            TipoTransaccion tipo,
            OffsetDateTime desde,
            OffsetDateTime hasta,
            Double minMonto,
            Double maxMonto,
            Integer page,
            Integer size,
            String sort,
            ServerWebExchange exchange) {

        // Convierte enums a String (o null) de forma segura
        final String estadoStr = (estado == null) ? null : estado.getValue();
        final String tipoStr   = (tipo   == null) ? null : tipo.getValue();

        // Llama al service con la firma existente (3 args)
        Flux<TransaccionResponse> body = transactionService
                .getFilteredHistory(cuentaId, estadoStr, tipoStr)
                .map(this::mapToResponse);

        return Mono.just(new ResponseEntity<>(body, HttpStatus.OK));
    }


    // --------- Mapper domain -> OpenAPI DTO ----------
    private TransaccionResponse mapToResponse(Transaction t) {
        TipoTransaccion tipo = t.getTipo() != null ? TipoTransaccion.fromValue(t.getTipo()) : null;
        EstadoTransaccion est = t.getEstado() != null ? EstadoTransaccion.fromValue(t.getEstado()) : null;

        OffsetDateTime fecha = null;
        if (t.getFecha() != null) {
            // Usa el offset de la zona actual (o fija UTC si prefieres)
            ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(t.getFecha());
            fecha = t.getFecha().atOffset(offset);
            // Si quieres UTC:
            // fecha = t.getFecha().atOffset(ZoneOffset.UTC);
        }

        return new TransaccionResponse()
                .id(t.getId())
                .tipo(tipo)
                .estado(est)
                .monto(t.getMonto())
                .fecha(fecha)                           // <-- ahora es OffsetDateTime
                .cuentaOrigenId(t.getCuentaOrigenId())
                .cuentaDestinoId(t.getCuentaDestinoId())
                .referencia(t.getReferencia());
    }
}
