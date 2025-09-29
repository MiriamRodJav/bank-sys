package com.transaction_ms.transaction_ms.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String tipo;
    private Double monto;
    private LocalDateTime  fecha;
    private String cuentaOrigenId;
    private String cuentaDestinoId;
    private String referencia;

    private String estado;
}
