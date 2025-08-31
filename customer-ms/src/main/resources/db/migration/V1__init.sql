CREATE TABLE clientes
(
    id       BINARY(16) NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni      VARCHAR(8)   NOT NULL,
    email    VARCHAR(255),
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uk_clientes_dni UNIQUE (dni)
);
