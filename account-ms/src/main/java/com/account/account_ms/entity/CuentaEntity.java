package com.account.account_ms.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cuentas")
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", unique = true, nullable = false)
    private String numeroCuenta;

    @Column(name = "saldo", nullable = false)
    private Double saldo;

    @Column(name = "tipo_cuenta", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCuentaEnum tipoCuenta;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    public enum TipoCuentaEnum {
        AHORROS,
        CORRIENTE
    }

}
