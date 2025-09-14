package com.customer.customer_ms.exceptions;

import com.customer.customer_ms.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleClienteDuplicado_returnsConflictAndMessage() {
        var ex = new ClienteDuplicadoException("El cliente con DNI duplicado ya existe");
        var resp = handler.handleClienteDuplicado(ex);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("409 CONFLICT", resp.getBody().getErrorCode());
        assertTrue(resp.getBody().getMessage().contains("DNI duplicado")); // <- en vez de equals
    }

    @Test
    void handleClientesNoEncontrados_returnsNotFoundAndMessage() {
        ClientesNoEncontradosException ex = new ClientesNoEncontradosException("No hay clientes");
        ResponseEntity<ErrorResponse> resp = handler.handleClientesNoEncontrados(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.NOT_FOUND.toString(), resp.getBody().getErrorCode()); // "404 NOT_FOUND"
        assertEquals("No hay clientes", resp.getBody().getMessage());
    }

    @Test
    void handleValidacion_returnsBadRequestAndMessage() {
        ValidacionException ex = new ValidacionException("Request inválido");
        ResponseEntity<ErrorResponse> resp = handler.handleValidacion(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), resp.getBody().getErrorCode()); // "400 BAD_REQUEST"
        assertEquals("Request inválido", resp.getBody().getMessage());
    }

    @Test
    void handleValidationErrors_returnsBadRequestWithDetails() throws NoSuchMethodException {
        // Arrange: simulamos errores de binding
        Object target = new Object();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(target, "clienteRequest");
        binding.addError(new FieldError("clienteRequest", "nombre", "no debe estar vacío"));
        binding.addError(new FieldError("clienteRequest", "dni", "debe tener 8 caracteres"));

        MethodParameter mp = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationErrors_returnsBadRequestWithDetails"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, binding);

        // Act
        ResponseEntity<ErrorResponse> resp = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), resp.getBody().getErrorCode());
        String msg = resp.getBody().getMessage();
        assertTrue(msg.startsWith("Error de validación: "));
        assertTrue(msg.contains("nombre: no debe estar vacío"));
        assertTrue(msg.contains("dni: debe tener 8 caracteres"));
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new RuntimeException("boom");
        ResponseEntity<ErrorResponse> resp = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), resp.getBody().getErrorCode()); // "500 INTERNAL_SERVER_ERROR"
        assertEquals("Ocurrió un error interno en el servidor", resp.getBody().getMessage());
    }

    @Test
    void handleClienteConCuentasActivas_returnsConflictAndMessage() {
        ClienteConCuentasActivasException ex = new ClienteConCuentasActivasException("Tiene cuentas activas");
        ResponseEntity<ErrorResponse> resp = handler.handleClienteConCuentasActivas(ex);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.CONFLICT.toString(), resp.getBody().getErrorCode());
        assertEquals("Tiene cuentas activas", resp.getBody().getMessage());
    }
}