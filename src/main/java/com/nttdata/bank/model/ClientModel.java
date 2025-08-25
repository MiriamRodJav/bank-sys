package com.nttdata.bank.model;

import lombok.*;

/**
 * Represents a client of the bank.
 * Contains basic personal information such as name,
 * identification number, and contact details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ClientModel {

    /** Unique identifier of the client. */
    private Long clientId;

    /** Client's first name. */
    private String firstName;

    /** Client's last name. */
    private String lastName;

    /** Client's national identity document (DNI). */
    private String dni;

    /** Client's email address. */
    private String email;
}