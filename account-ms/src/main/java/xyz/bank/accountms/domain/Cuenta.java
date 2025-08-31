package xyz.bank.accountms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cuentas",
        uniqueConstraints = @UniqueConstraint(name = "uk_cuentas_numero", columnNames = "numero_cuenta"),
        indexes = @Index(name = "ix_cuentas_cliente", columnList = "cliente_id"))
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "numero_cuenta", nullable = false, length = 24)
    private String numeroCuenta;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 16)
    private TipoCuenta tipoCuenta;

    @Column(name = "cliente_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID clienteId;
}