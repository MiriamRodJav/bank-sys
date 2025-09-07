package com.transaction_ms.transaction_ms.domain;

import com.transaction_ms.transaction_ms.model.EstadoTransaccion;
import com.transaction_ms.transaction_ms.model.TipoTransaccion;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
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
