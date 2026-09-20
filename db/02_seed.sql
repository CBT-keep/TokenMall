-- TokenMall seed data for MySQL 8.0.34
-- This script is safe to run more than once because it uses INSERT IGNORE.

SET NAMES utf8mb4;
SET time_zone = '+08:00';

USE token_mall;

INSERT IGNORE INTO sys_user (
  id, username, password_hash, nickname, email, phone, role, status, deleted
) VALUES (
  1,
  'admin',
  '$2b$10$laqc8KkOuKgZnZw1m2LHGuww0xR3Lvk4fi20y6SSeIZC.VfX3jgCS',
  '系统管理员',
  'admin@tokenmall.local',
  NULL,
  'ADMIN',
  1,
  0
);

INSERT IGNORE INTO user_token_account (
  id, user_id, pack_balance, plan_balance, total_purchased, total_consumed
) VALUES (
  1, 1, 0, 0, 0, 0
);

INSERT IGNORE INTO product_category (id, name, code, sort_order, status, deleted) VALUES
  (1, 'Token 资源包', 'TOKEN_PACK', 10, 1, 0),
  (2, 'Token Plan', 'TOKEN_PLAN', 20, 1, 0);

INSERT IGNORE INTO product (
  id, category_id, product_type, name, subtitle, description, cover_url,
  price, original_price, token_amount, plan_days, plan_quota, purchase_limit,
  status, sort_order, deleted
) VALUES
  (
    101, 1, 'TOKEN_PACK', '1,000 Token 资源包', '适合小规模体验',
    '一次性购买，支付成功后 1,000 Token 进入资源包余额。',
    NULL, 9.90, 12.00, 1000, NULL, NULL, 0, 1, 10, 0
  ),
  (
    102, 1, 'TOKEN_PACK', '5,000 Token 资源包', '适合日常学习',
    '一次性购买，支付成功后 5,000 Token 进入资源包余额。',
    NULL, 39.90, 49.00, 5000, NULL, NULL, 0, 1, 20, 0
  ),
  (
    103, 1, 'TOKEN_PACK', '20,000 Token 资源包', '适合高频使用',
    '一次性购买，支付成功后 20,000 Token 进入资源包余额。',
    NULL, 139.90, 179.00, 20000, NULL, NULL, 0, 1, 30, 0
  ),
  (
    104, 2, 'TOKEN_PLAN', 'Starter Plan', '30 天 50,000 Token 配额',
    '支付成功后获得 30 天有效期的 50,000 Token Plan 配额。',
    NULL, 29.90, 39.00, NULL, 30, 50000, 1, 1, 10, 0
  ),
  (
    105, 2, 'TOKEN_PLAN', 'Pro Plan', '30 天 200,000 Token 配额',
    '支付成功后获得 30 天有效期的 200,000 Token Plan 配额。',
    NULL, 99.00, 129.00, NULL, 30, 200000, 1, 1, 20, 0
  ),
  (
    106, 2, 'TOKEN_PLAN', 'Max Plan', '30 天 1,000,000 Token 配额',
    '限量秒杀 Plan，支付成功后获得 30 天有效期的 1,000,000 Token 配额。',
    NULL, 399.00, 499.00, NULL, 30, 1000000, 1, 1, 30, 0
  );

INSERT IGNORE INTO product_sku (
  id, product_id, sku_code, name, price, token_amount, plan_days, plan_quota, status, deleted
) VALUES
  (1001, 101, 'TP-1000', '1,000 Tokens', 9.90, 1000, NULL, NULL, 1, 0),
  (1002, 102, 'TP-5000', '5,000 Tokens', 39.90, 5000, NULL, NULL, 1, 0),
  (1003, 103, 'TP-20000', '20,000 Tokens', 139.90, 20000, NULL, NULL, 1, 0),
  (1004, 104, 'PLAN-STARTER-30D-50K', 'Starter Plan 30 天', 29.90, NULL, 30, 50000, 1, 0),
  (1005, 105, 'PLAN-PRO-30D-200K', 'Pro Plan 30 天', 99.00, NULL, 30, 200000, 1, 0),
  (1006, 106, 'PLAN-MAX-30D-1M', 'Max Plan 30 天', 399.00, NULL, 30, 1000000, 1, 0);

INSERT IGNORE INTO inventory (
  id, sku_id, total_stock, available_stock, locked_stock, version
) VALUES
  (2001, 1001, 100000, 100000, 0, 0),
  (2002, 1002, 50000, 50000, 0, 0),
  (2003, 1003, 10000, 10000, 0, 0),
  (2004, 1004, 5000, 5000, 0, 0),
  (2005, 1005, 1000, 1000, 0, 0),
  (2006, 1006, 500, 500, 0, 0);

INSERT IGNORE INTO seckill_activity (
  id, product_id, sku_id, name, seckill_price, seckill_stock, sold_count,
  per_user_limit, start_time, end_time, status, deleted
) VALUES (
  1,
  106,
  1006,
  'Max Plan 限时秒杀',
  199.00,
  100,
  0,
  1,
  DATE_SUB(NOW(), INTERVAL 1 HOUR),
  DATE_ADD(NOW(), INTERVAL 30 DAY),
  'RUNNING',
  0
);

-- Default administrator:
-- username: admin
-- password: admin123
-- Change this password after the first login in a real environment.
