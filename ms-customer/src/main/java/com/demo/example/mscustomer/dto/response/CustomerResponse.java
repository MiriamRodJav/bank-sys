package com.demo.example.mscustomer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerResponse {
    private Integer id;
    private String name;
    private String lastName;
    private String dni;
    private String email;
}
