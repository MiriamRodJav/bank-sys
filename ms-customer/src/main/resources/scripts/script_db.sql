CREATE DATABASE BANKSYS2;
USE BANKSYS2;

CREATE TABLE customer (
    id_customer INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    dni        VARCHAR(20) UNIQUE NOT NULL,
    email      VARCHAR(150) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bank_account (
    id_account INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL,
    balance DOUBLE NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    customer_id INT NOT NULL,
    CONSTRAINT fk_client FOREIGN KEY (customer_id) REFERENCES customer(id_customer)
);

select * from customer;
select * from bank_account;

