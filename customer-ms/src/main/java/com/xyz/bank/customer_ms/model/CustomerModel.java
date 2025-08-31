package com.xyz.bank.customer_ms.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerModel {

    private Long id;

    private String firstName;

    private String lastName;

    private String dni;

    private String email;

}