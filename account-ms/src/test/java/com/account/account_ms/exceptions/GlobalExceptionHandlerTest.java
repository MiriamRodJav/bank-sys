package com.account.account_ms.exceptions;

import com.account.account_ms.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest mockRequest;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
    }

    @Test
    void handleSaldoInsuficienteException_returnsBadRequestAndMessage() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException("Saldo insuficiente para cuenta.");
        ResponseEntity<ErrorResponse> resp = handler.handleSaldoInsuficienteException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), resp.getBody().getErrorCode());
        assertEquals("Saldo insuficiente para cuenta.", resp.getBody().getMessage());
    }

    @Test
    void handleCuentaNoEncontradaException_returnsNotFoundAndMessage() {
        CuentaNoEncontradaException ex = new CuentaNoEncontradaException("Cuenta no encontrada.");
        ResponseEntity<ErrorResponse> resp = handler.handleCuentaNoEncontradaException(ex, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.NOT_FOUND.toString(), resp.getBody().getErrorCode());
        assertEquals("Cuenta no encontrada.", resp.getBody().getMessage());
    }

    @Test
    void handleClienteNoEncontradoException_returnsNotFoundAndMessage() {
        ClienteNoEncontradoException ex = new ClienteNoEncontradoException("Cliente no encontrado.");
        ResponseEntity<ErrorResponse> resp = handler.handleClienteNoEncontradoException(ex, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.NOT_FOUND.toString(), resp.getBody().getErrorCode());
        assertEquals("Cliente no encontrado.", resp.getBody().getMessage());
    }

    @Test
    void handleValidacionCuentaException_returnsBadRequestAndMessage() {
        ValidacionCuentaException ex = new ValidacionCuentaException("Validación inválida.");
        ResponseEntity<ErrorResponse> resp = handler.handleValidacionCuentaException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), resp.getBody().getErrorCode());
        assertEquals("Validación inválida.", resp.getBody().getMessage());
    }


}
