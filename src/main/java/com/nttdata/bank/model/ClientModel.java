package com.nttdata.bank.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ClientModel {

    public Long id;

    public String firstName;

    public String lastName;

    public String dni;

    public String email;

}
