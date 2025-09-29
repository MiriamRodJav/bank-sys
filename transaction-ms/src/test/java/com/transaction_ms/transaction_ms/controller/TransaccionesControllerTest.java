package com.transaction_ms.transaction_ms.controller;

import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.mapper.TransactionMapper;
import com.transaction_ms.transaction_ms.model.*;
import com.transaction_ms.transaction_ms.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = TransaccionesController.class)
public class TransaccionesControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionMapper transactionMapper;

    private final DepositoRequest depositRequest = new DepositoRequest("CUENTA123", 100.0);
    private final RetiroRequest withdrawRequest = new RetiroRequest("CUENTA456", 50.0);
    private final TransferenciaRequest transferRequest = new TransferenciaRequest("ORIGEN",
            "DESTINO", 200.0);

    private Transaction depositDomain;
    private Transaction withdrawDomain;
    private Transaction transferDomain;

    private TransaccionResponse depositResponse;
    private TransaccionResponse withdrawResponse;
    private TransaccionResponse transferResponse;


    @BeforeEach
    void setUp() {
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        depositDomain = new Transaction("T1", "DEPOSIT", 100.0, LocalDateTime.now(),
                "CUENTA123", null, "REF1", "SUCCESS");
        withdrawDomain = new Transaction("T2", "WITHDRAW", 50.0, LocalDateTime.now(),
                "CUENTA456", null, "REF2", "SUCCESS");
        transferDomain = new Transaction("T3", "TRANSFER", 200.0, LocalDateTime.now(),
                "ORIGEN", "DESTINO", "REF3", "SUCCESS");

        depositResponse = new TransaccionResponse()
                .id("T1")
                .tipo(TipoTransaccion.DEPOSITO)
                .monto(100.0)
                .fecha(formattedDate)
                .estado(EstadoTransaccion.SUCCESS)
                .cuentaOrigenId("CUENTA123");

        withdrawResponse = new TransaccionResponse()
                .id("T2")
                .tipo(TipoTransaccion.RETIRO)
                .monto(50.0)
                .fecha(formattedDate)
                .estado(EstadoTransaccion.SUCCESS)
                .cuentaOrigenId("CUENTA456");

        transferResponse = new TransaccionResponse()
                .id("T3")
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .monto(200.0)
                .fecha(formattedDate)
                .estado(EstadoTransaccion.SUCCESS)
                .cuentaOrigenId("ORIGEN")
                .cuentaDestinoId("DESTINO");


        when(transactionMapper.toResponse(any(Transaction.class)))
                .thenReturn(depositResponse);
    }

    @Test
    void depositar_Success_ShouldReturn201Created() {
        when(transactionService.processDeposit(any(DepositoRequest.class)))
                .thenReturn(Mono.just(depositDomain));

        when(transactionMapper.toResponse(depositDomain)).thenReturn(depositResponse);

        webTestClient.post().uri("/transacciones/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(depositRequest)
                .exchange()
                .expectStatus().isCreated() // Verifica HTTP 201
                .expectBody(TransaccionResponse.class)
                .value(response -> {
                    assertEquals(depositResponse.getId(), response.getId());
                    assertEquals(TipoTransaccion.DEPOSITO, response.getTipo());
                });

        verify(transactionService, times(1)).processDeposit(any(DepositoRequest.class));
        verify(transactionMapper, times(1)).toResponse(depositDomain);
    }


    @Test
    void retirar_Success_ShouldReturn201Created() {
        when(transactionService.processWithdraw(any(RetiroRequest.class)))
                .thenReturn(Mono.just(withdrawDomain));

        when(transactionMapper.toResponse(withdrawDomain)).thenReturn(withdrawResponse);

        webTestClient.post().uri("/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(withdrawRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransaccionResponse.class)
                .value(response -> assertEquals(withdrawResponse.getId(), response.getId()));

        verify(transactionService, times(1)).processWithdraw(any(RetiroRequest.class));
        verify(transactionMapper, times(1)).toResponse(withdrawDomain);
    }

    @Test
    void transferir_Success_ShouldReturn201Created() {
        when(transactionService.processTransfer(any(TransferenciaRequest.class)))
                .thenReturn(Mono.just(transferDomain));

        when(transactionMapper.toResponse(transferDomain)).thenReturn(transferResponse);

        webTestClient.post().uri("/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transferRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransaccionResponse.class)
                .value(response -> assertEquals(transferResponse.getId(), response.getId()));

        verify(transactionService, times(1)).processTransfer(any(TransferenciaRequest.class));
        verify(transactionMapper, times(1)).toResponse(transferDomain);
    }

    @Test
    void historial_WithFilters_ShouldReturn200OkAndFluxOfResponses() {
        Transaction t1 = depositDomain;
        Transaction t2 = withdrawDomain;
        TransaccionResponse r1 = depositResponse;
        TransaccionResponse r2 = withdrawResponse;

        when(transactionService.getTransactionHistory(eq("SUCCESS"), eq("DEPOSITO")))
                .thenReturn(Flux.just(t1, t2));

        when(transactionMapper.toResponse(t1)).thenReturn(r1);
        when(transactionMapper.toResponse(t2)).thenReturn(r2);

        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/transacciones/historial")
                        .queryParam("estado", EstadoTransaccion.SUCCESS)
                        .queryParam("tipo", TipoTransaccion.DEPOSITO)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(TransaccionResponse.class)
                .hasSize(2)
                .contains(r1, r2);

        verify(transactionService, times(1))
                .getTransactionHistory("SUCCESS", "DEPOSITO");
        verify(transactionMapper, times(2))
                .toResponse(any(Transaction.class));
    }

    @Test
    void historial_WithoutFilters_ShouldReturn200Ok() {
        when(transactionService.getTransactionHistory(isNull(), isNull()))
                .thenReturn(Flux.empty());

        webTestClient.get().uri("/transacciones/historial")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransaccionResponse.class)
                .hasSize(0);

        verify(transactionService, times(1)).getTransactionHistory(isNull(), isNull());
    }
}