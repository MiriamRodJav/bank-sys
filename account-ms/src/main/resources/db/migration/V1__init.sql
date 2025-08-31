CREATE TABLE cuentas
(
    id            BINARY(16) NOT NULL,
    numero_cuenta VARCHAR(24)    NOT NULL,
    saldo         DECIMAL(18, 2) NOT NULL,
    tipo_cuenta   VARCHAR(16)    NOT NULL,
    cliente_id    BINARY(16) NOT NULL,
    CONSTRAINT pk_cuentas PRIMARY KEY (id),
    CONSTRAINT uk_cuentas_numero UNIQUE (numero_cuenta),
    KEY           ix_cuentas_cliente (cliente_id)
);