-- TokenMall schema for MySQL 8.0.34
-- Execute this script with an account that can create databases and tables.
-- This script is intentionally written with CREATE TABLE IF NOT EXISTS.
-- It does not drop existing data.

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS token_mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE token_mall;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  username VARCHAR(50) NOT NULL COMMENT 'Login name',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
  nickname VARCHAR(50) NOT NULL COMMENT 'Display name',
  email VARCHAR(100) NULL COMMENT 'Email',
  phone VARCHAR(20) NULL COMMENT 'Phone number',
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER or ADMIN',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Users';

CREATE TABLE IF NOT EXISTS user_token_account (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  pack_balance BIGINT NOT NULL DEFAULT 0 COMMENT 'Token balance from resource packs',
  plan_balance BIGINT NOT NULL DEFAULT 0 COMMENT 'Token balance from active plans',
  total_purchased BIGINT NOT NULL DEFAULT 0,
  total_consumed BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User token account';

CREATE TABLE IF NOT EXISTS product_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  code VARCHAR(50) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Product categories';

CREATE TABLE IF NOT EXISTS product (
  id BIGINT NOT NULL AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  product_type VARCHAR(32) NOT NULL COMMENT 'TOKEN_PACK or TOKEN_PLAN',
  name VARCHAR(100) NOT NULL,
  subtitle VARCHAR(200) NULL,
  description TEXT NULL,
  cover_url VARCHAR(500) NULL,
  price DECIMAL(10, 2) NOT NULL COMMENT 'Display price',
  original_price DECIMAL(10, 2) NULL,
  token_amount BIGINT NULL COMMENT 'Token amount for TOKEN_PACK',
  plan_days INT NULL COMMENT 'Validity days for TOKEN_PLAN',
  plan_quota BIGINT NULL COMMENT 'Quota for TOKEN_PLAN',
  purchase_limit INT NOT NULL DEFAULT 0 COMMENT '0 means unlimited',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 off shelf, 1 on shelf',
  sort_order INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category_status (category_id, status, deleted),
  KEY idx_type_status_sort (product_type, status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Products';

CREATE TABLE IF NOT EXISTS product_sku (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_code VARCHAR(64) NOT NULL,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  token_amount BIGINT NULL,
  plan_days INT NULL,
  plan_quota BIGINT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sku_code (sku_code),
  KEY idx_product_status (product_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Product SKUs';

CREATE TABLE IF NOT EXISTS inventory (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sku_id BIGINT NOT NULL,
  total_stock INT NOT NULL DEFAULT 0,
  available_stock INT NOT NULL DEFAULT 0,
  locked_stock INT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0 COMMENT 'Reserved for optimistic locking',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU inventory';

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  selected TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_sku (user_id, sku_id),
  KEY idx_user_selected (user_id, selected)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Shopping cart items';

CREATE TABLE IF NOT EXISTS mall_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  order_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL or SECKILL',
  activity_id BIGINT NULL,
  total_amount DECIMAL(10, 2) NOT NULL,
  pay_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
  request_id VARCHAR(64) NULL,
  pay_time DATETIME NULL,
  expire_time DATETIME NOT NULL,
  cancel_reason VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_request_id (request_id),
  KEY idx_user_status_created (user_id, status, created_at),
  KEY idx_status_expire (status, expire_time),
  KEY idx_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Orders';

CREATE TABLE IF NOT EXISTS mall_order_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(32) NOT NULL,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  product_type VARCHAR(32) NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  sku_name VARCHAR(100) NOT NULL,
  unit_price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL,
  token_amount BIGINT NULL,
  plan_days INT NULL,
  plan_quota BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_id (order_id),
  KEY idx_order_no (order_no),
  KEY idx_product_sku (product_id, sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Order item snapshots';

CREATE TABLE IF NOT EXISTS order_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NOT NULL,
  operator_type VARCHAR(20) NOT NULL COMMENT 'USER, ADMIN, SYSTEM, MQ',
  remark VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_created (order_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Order status transitions';

CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payment_no VARCHAR(32) NOT NULL,
  order_no VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  channel VARCHAR(20) NOT NULL DEFAULT 'MOCK',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  request_id VARCHAR(64) NULL,
  paid_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_no (payment_no),
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_request_id (request_id),
  KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mock payment records';

CREATE TABLE IF NOT EXISTS user_token_plan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  total_quota BIGINT NOT NULL,
  used_quota BIGINT NOT NULL DEFAULT 0,
  remaining_quota BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, EXPIRED or EXHAUSTED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_status (user_id, status),
  KEY idx_end_time (status, end_time),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User token plans';

CREATE TABLE IF NOT EXISTS token_transaction (
  id BIGINT NOT NULL AUTO_INCREMENT,
  transaction_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT NULL,
  plan_id BIGINT NULL,
  balance_type VARCHAR(20) NOT NULL COMMENT 'PACK or PLAN',
  transaction_type VARCHAR(32) NOT NULL COMMENT 'PACK_PURCHASE, PLAN_PURCHASE, CONSUME or REFUND',
  amount BIGINT NOT NULL COMMENT 'Positive for credit, negative for consume',
  balance_after BIGINT NOT NULL,
  idempotency_key VARCHAR(128) NULL COMMENT 'Unique key for message idempotency',
  description VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_transaction_no (transaction_no),
  UNIQUE KEY uk_idempotency_key (idempotency_key),
  KEY idx_user_created (user_id, created_at),
  KEY idx_order_id (order_id),
  KEY idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Token transactions';

CREATE TABLE IF NOT EXISTS token_usage_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plan_id BIGINT NULL,
  amount BIGINT NOT NULL,
  request_id VARCHAR(64) NOT NULL,
  description VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_request_id (request_id),
  KEY idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Token usage records';

CREATE TABLE IF NOT EXISTS seckill_activity (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  seckill_price DECIMAL(10, 2) NOT NULL,
  seckill_stock INT NOT NULL,
  sold_count INT NOT NULL DEFAULT 0,
  per_user_limit INT NOT NULL DEFAULT 1,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT, READY, RUNNING, ENDED or CANCELLED',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_status_time (status, start_time, end_time),
  KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Seckill activities';

CREATE TABLE IF NOT EXISTS seckill_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_no VARCHAR(32) NULL,
  request_id VARCHAR(64) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED' COMMENT 'ACCEPTED, SUCCESS, FAILED or CANCELLED',
  error_message VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_user (activity_id, user_id),
  UNIQUE KEY uk_request_id (request_id),
  KEY idx_activity_status (activity_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Seckill requests';

CREATE TABLE IF NOT EXISTS message_outbox (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id VARCHAR(64) NOT NULL,
  payload JSON NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT 'NEW, SENT or FAILED',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME NULL,
  error_message VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_event_id (event_id),
  KEY idx_status_retry (status, next_retry_at),
  KEY idx_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Transactional outbox';

CREATE TABLE IF NOT EXISTS idempotency_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  scope VARCHAR(64) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  request_hash VARCHAR(128) NOT NULL,
  response_json JSON NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING, SUCCESS or FAILED',
  expire_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_scope_key_user (scope, idempotency_key, user_id),
  KEY idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='HTTP idempotency records';

CREATE TABLE IF NOT EXISTS mq_consume_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  consumer_group VARCHAR(100) NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  message_id VARCHAR(100) NULL,
  event_type VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING, SUCCESS or FAILED',
  retry_count INT NOT NULL DEFAULT 0,
  error_message VARCHAR(500) NULL,
  consumed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_consumer_event (consumer_group, event_id),
  KEY idx_event_type_status (event_type, status),
  KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MQ consumer idempotency log';

-- Learning checkpoints:
-- MYSQL-03: inventory updates intentionally have no optimistic lock usage in baseline code.
-- MYSQL-04: payment and token grant idempotency will be implemented by the learner.
-- MYSQL-05: order close and payment concurrency must be verified by experiments.
