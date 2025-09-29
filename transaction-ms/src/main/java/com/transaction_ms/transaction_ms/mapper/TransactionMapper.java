package com.transaction_ms.transaction_ms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.model.TransaccionResponse;
import com.transaction_ms.transaction_ms.model.TipoTransaccion;
import com.transaction_ms.transaction_ms.model.EstadoTransaccion;

@Mapper(componentModel = "spring")

public interface TransactionMapper {
    @Mapping(target = "tipo", expression = "java(mapTipo(transaction.getTipo()))")
    @Mapping(target = "estado", expression = "java(mapEstado(transaction.getEstado()))")
    @Mapping(target = "fecha",
            expression = "java(transaction.getFecha().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))")
    TransaccionResponse toResponse(Transaction transaction);

    default TipoTransaccion mapTipo(String tipo) {
        return (tipo != null) ? TipoTransaccion.fromValue(tipo) : null;
    }

    default EstadoTransaccion mapEstado(String estado) {
        return (estado != null) ? EstadoTransaccion.fromValue(estado) : null;
    }

}
