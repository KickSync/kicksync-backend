SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE settlements;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE products;
TRUNCATE TABLE partners;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

SET SESSION cte_max_recursion_depth = 1000000;

INSERT INTO users (username, password, role, created_at, updated_at)
WITH RECURSIVE cte AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM cte WHERE n < 10000
)
SELECT 
    CONCAT('partner_user_', n), 'hashed_pw', 'USER', NOW(), NOW()
FROM cte;

SET @first_user_id = (SELECT MIN(id) FROM users);

INSERT INTO partners (name, business_number, commission_rate, contact_email, bank_name, account_number, account_holder, user_id, created_at, updated_at)
WITH RECURSIVE cte AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM cte WHERE n < 10000
)
SELECT
    CONCAT('Partner_', n), CONCAT('123-45-', LPAD(n, 5, '0')), 0.0500,
    CONCAT('partner', n, '@kicksync.com'), 'TestBank', CONCAT('1000-2000-', n), CONCAT('Holder_', n),
    @first_user_id + (n - 1),
    NOW(), NOW()
FROM cte;

SET @first_partner_id = (SELECT MIN(id) FROM partners);

INSERT INTO orders (final_price, order_date, status, user_id, receiver_name, receiver_phone, partner_id, merchant_uid, detail, street, zipcode, created_at, updated_at)
WITH RECURSIVE cte AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM cte WHERE n < 1000000
)
SELECT
    15000.00,
    NOW() - INTERVAL 1 DAY,
    'PURCHASE_CONFIRMED',
    @first_user_id,
    CONCAT('Receiver_', n),
    '010-0000-0000',
    @first_partner_id + (n % 10000),
    CONCAT('MOCK_UID_', n),
    'Seoul', 'Teheran-ro', '06236',
    NOW(), NOW()
FROM cte;
