package com.nttdata.bank.model;

import lombok.*;

import java.util.regex.Pattern;

@Data
public class ClientModel {

    private String dni;
    private String firstName;
    private String lastName;
    private String email;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public ClientModel(String dni , String lastName, String firstName, String email) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    public ClientModel() {

    }
    public boolean validateEmail() {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean validateDni() {
        return dni != null && !dni.trim().isEmpty() && dni.length() >= 8;
    }
}

