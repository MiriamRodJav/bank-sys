package com.transaction_ms.transaction_ms.exception;

import com.transaction_ms.transaction_ms.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException ex) {

        ErrorResponse errorBody = new ErrorResponse();
        errorBody.setErrorCode(ex.getErrorCode());
        errorBody.setMessage(ex.getMessage());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(errorBody);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {


        ErrorResponse errorBody = new ErrorResponse();
        errorBody.setErrorCode("GEN_500_RUNTIME");
        errorBody.setMessage("Ocurrió un error inesperado en el servidor: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse errorBody = new ErrorResponse();
        errorBody.setErrorCode("GEN_500_SERVER");
        errorBody.setMessage("Error interno del servidor: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody);
    }
}
