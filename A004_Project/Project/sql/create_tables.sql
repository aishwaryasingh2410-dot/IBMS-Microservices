CREATE TABLE IF NOT EXISTS auth_db.users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transaction_db.accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(40) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    INDEX idx_accounts_user_id (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transaction_db.transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    destination_account_id BIGINT,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_transactions_account_id (account_id),
    INDEX idx_transactions_destination_account_id (destination_account_id),
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES transaction_db.accounts (id),
    CONSTRAINT fk_transactions_destination
        FOREIGN KEY (destination_account_id) REFERENCES transaction_db.accounts (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification_db.notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    message VARCHAR(255),
    type VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS loan_db.loan_applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    amount DOUBLE NOT NULL,
    status VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
