package com.customer.customer_ms.exceptions;

public class ClienteDuplicadoException extends RuntimeException{
    public ClienteDuplicadoException(String dni) {
        super("El cliente con DNI " + dni + " ya existe");
    }
}
