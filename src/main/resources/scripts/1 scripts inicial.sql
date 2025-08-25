-- ======================================
-- TABLE CLIENT
-- ======================================
CREATE TABLE clients (
    dni VARCHAR(8) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================================
-- TABLE BANK ACCOUNT
-- ======================================
CREATE TABLE bank_accounts (
    account_number VARCHAR(30) PRIMARY KEY,
    balance DECIMAL(15,2) DEFAULT 0.00 CHECK (balance >= -500.00),
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    client_dni VARCHAR(8) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_dni) REFERENCES clients(dni) ON DELETE CASCADE,
    INDEX idx_client_dni (client_dni)
);