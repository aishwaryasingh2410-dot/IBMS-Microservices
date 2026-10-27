-- Demo account credentials: demo@example.com / DemoPassword123!
-- The BCrypt password hash is generated for the documented demo password.
INSERT INTO auth_db.users (id, name, email, password, phone)
VALUES (
    1,
    'Demo User',
    'demo@example.com',
    '$2a$10$0tgURLhLPtrD5vypZogs.OczrM9U5YYLNJOe3f1NdqO07.4BuwSPu',
    '555-0100'
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    phone = VALUES(phone);

INSERT INTO transaction_db.accounts (id, user_id, account_number, balance)
VALUES (1, 1, 'IBMS-DEMO-0001', 250.00)
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    account_number = VALUES(account_number),
    balance = VALUES(balance);

INSERT INTO transaction_db.transactions
    (id, account_id, destination_account_id, type, amount, status)
VALUES (1, 1, NULL, 'DEPOSIT', 250.00, 'SUCCESS')
ON DUPLICATE KEY UPDATE
    account_id = VALUES(account_id),
    destination_account_id = VALUES(destination_account_id),
    type = VALUES(type),
    amount = VALUES(amount),
    status = VALUES(status);

INSERT INTO notification_db.notifications (id, user_id, message, type)
VALUES (1, 1, 'Welcome to IBMS demo banking', 'ACCOUNT')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    message = VALUES(message),
    type = VALUES(type);

INSERT INTO loan_db.loan_applications (id, user_id, amount, status)
VALUES (1, 1, 5000.00, 'PENDING')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    amount = VALUES(amount),
    status = VALUES(status);
