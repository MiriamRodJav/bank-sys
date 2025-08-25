-- ======================================
-- SCHEMA
-- ======================================
CREATE SCHEMA `bank-sys` ;

-- ======================================
-- TABLE CLIENT
-- ======================================
CREATE TABLE Client (
    clientId INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    CONSTRAINT chk_email CHECK (email LIKE '_%@_%._%')
);

-- ======================================
-- TABLE BANK ACCOUNT
-- ======================================
CREATE TABLE BankAccount (
    accountId INT AUTO_INCREMENT PRIMARY KEY,
    accountNumber VARCHAR(30) NOT NULL UNIQUE,
    balance DOUBLE DEFAULT 0.0,
    accountType ENUM('SAVINGS', 'CHECKING') NOT NULL,
    clientId INT NOT NULL,
    FOREIGN KEY (clientId) REFERENCES Client(clientId),
    CONSTRAINT chk_balance CHECK (
        (accountType = 'SAVINGS' AND balance >= 0.0)
        OR
        (accountType = 'CHECKING' AND balance >= -500.0)
    )
);