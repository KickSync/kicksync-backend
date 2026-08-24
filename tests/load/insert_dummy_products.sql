-- ===================================================================
--      대용량 상품 더미 데이터 삽입 스크립트 (100,000개 상품)
-- ===================================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE products;
SET FOREIGN_KEY_CHECKS = 1;

-- MySQL 재귀 한도 확장
SET SESSION cte_max_recursion_depth = 1000000;

-- 기존 파트너가 없는 경우를 대비한 디폴트 파트너 생성
INSERT INTO users (id, username, password, role, created_at, updated_at)
SELECT 999999, 'default_partner_user', 'hashed_pw', 'PARTNER', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'default_partner_user');

INSERT INTO partners (id, name, business_number, commission_rate, contact_email, bank_name, account_number, account_holder, user_id, created_at, updated_at)
SELECT 999999, 'Default Partner', '000-00-00000', 0.05, 'partner@kicksync.com', 'TestBank', '123-456', 'Holder', 999999, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM partners LIMIT 1);

SET @first_partner_id = (SELECT MIN(id) FROM partners);
SET @fallback_partner_id = (SELECT MIN(id) FROM partners WHERE name = 'Default Partner');

-- 100,000개의 대용량 상품 데이터 주입
INSERT INTO products (name, model, release_date, retail_price, stock, partner_id, created_at, updated_at)
WITH RECURSIVE cte AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM cte WHERE n < 100000
)
SELECT 
    CONCAT('KickSync Premium Sneaker v', n),
    CONCAT('KS-SNEAKER-', LPAD(n, 6, '0')),
    -- 2026-01-01부터 180일 동안의 다양한 출시일 분포 생성 (인덱스 탐색용)
    DATE_ADD('2026-01-01', INTERVAL (n % 180) DAY),
    150000.00 + (n % 1000) * 100,
    100,
    COALESCE(@first_partner_id + (n % 10000), @fallback_partner_id),
    NOW(), NOW()
FROM cte;

-- 주입된 레코드 수 검증
SELECT COUNT(*) AS 'Total Products Inserted' FROM products;
