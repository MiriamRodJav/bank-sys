package com.transaction_ms.transaction_ms.exception;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Data
public class TransactionException extends RuntimeException{
    private final String errorCode;
    private final HttpStatus httpStatus;

    public TransactionException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
