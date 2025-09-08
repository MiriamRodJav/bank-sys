package com.transaction_ms.transaction_ms.exception;

import com.transaction_ms.transaction_ms.model.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException ex) {
        ErrorResponse errorResponse = new ErrorResponse()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
}
