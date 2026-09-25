-- TokenMall 测试数据清理脚本
-- 用途：清理 JMeter 压测和验证脚本产生的用户、订单、Token 记录和秒杀记录，
--       并把库存、销量和 Token 余额恢复到干净状态。
-- 说明：这是手工维护脚本，不属于 01/02/03 初始化流程。
--
-- 执行前先备份：
--   mysqldump -u root -p token_mall > token_mall_backup.sql
--
-- 执行：
--   mysql -u token_mall_dev -p token_mall < db/04_cleanup_test_data.sql

USE token_mall;

START TRANSACTION;

-- 1. 目标测试用户：压测用户 + 验证脚本用户。admin 和 user 保留。
DROP TEMPORARY TABLE IF EXISTS tmp_test_users;
CREATE TEMPORARY TABLE tmp_test_users AS
SELECT id FROM sys_user
WHERE username LIKE 'tokenmall_load_%'
   OR username LIKE 'verify\_%';

-- 2. 这些用户的订单
DROP TEMPORARY TABLE IF EXISTS tmp_test_orders;
CREATE TEMPORARY TABLE tmp_test_orders AS
SELECT order_no FROM mall_order WHERE user_id IN (SELECT id FROM tmp_test_users);

-- 3. 删除订单相关数据，先子表后主表
DELETE FROM order_status_log WHERE order_no IN (SELECT order_no FROM tmp_test_orders);
DELETE FROM mall_order_item  WHERE order_no IN (SELECT order_no FROM tmp_test_orders);
DELETE FROM payment_record   WHERE order_no IN (SELECT order_no FROM tmp_test_orders);
DELETE FROM mall_order       WHERE order_no IN (SELECT order_no FROM tmp_test_orders);

-- 4. 删除测试用户的 Token、购物车、秒杀记录和账号
DELETE FROM user_token_plan     WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM token_usage_record  WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM token_transaction   WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM user_token_account  WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM cart_item           WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM seckill_record      WHERE user_id IN (SELECT id FROM tmp_test_users);
DELETE FROM sys_user            WHERE id IN (SELECT id FROM tmp_test_users);

-- 5. 删除剩下的人工测试订单。想保留 admin / user 的历史订单，就把这一段注释掉。
DELETE FROM order_status_log WHERE order_no IN (SELECT order_no FROM mall_order);
DELETE FROM mall_order_item  WHERE order_no IN (SELECT order_no FROM mall_order);
DELETE FROM payment_record   WHERE order_no IN (SELECT order_no FROM mall_order);
DELETE FROM mall_order;

-- 6. 重置 Token 账务，只保留 admin 和 user 两个账号
DELETE FROM user_token_plan;
DELETE FROM token_usage_record;
DELETE FROM token_transaction;
UPDATE user_token_account
SET pack_balance = 0,
    plan_balance = 0,
    total_purchased = 0,
    total_consumed = 0;

-- 7. 清空消息幂等和发件箱残留
DELETE FROM message_outbox;
DELETE FROM mq_consume_log;
DELETE FROM idempotency_record;

-- 8. 清空秒杀记录并重置销量，让活动可以重新压测
DELETE FROM seckill_record;
UPDATE seckill_activity SET sold_count = 0;

-- 可选：把活动 1 的库存改成一个方便压测的值
-- UPDATE seckill_activity SET seckill_stock = 100 WHERE id = 1;

-- 可选：删除验证用的活动 2
-- DELETE FROM seckill_activity WHERE id = 2;

-- 9. 恢复 SKU 可用库存到初始值
UPDATE inventory
SET available_stock = total_stock,
    locked_stock = 0,
    version = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_test_orders;
DROP TEMPORARY TABLE IF EXISTS tmp_test_users;

COMMIT;

-- 10. 可选：重置自增，让下一次测试的 id 从干净值开始
ALTER TABLE sys_user           AUTO_INCREMENT = 3;
ALTER TABLE user_token_account AUTO_INCREMENT = 3;
ALTER TABLE mall_order         AUTO_INCREMENT = 1;
ALTER TABLE mall_order_item    AUTO_INCREMENT = 1;
ALTER TABLE order_status_log   AUTO_INCREMENT = 1;
ALTER TABLE payment_record     AUTO_INCREMENT = 1;
ALTER TABLE token_transaction  AUTO_INCREMENT = 1;
ALTER TABLE user_token_plan    AUTO_INCREMENT = 1;
ALTER TABLE token_usage_record AUTO_INCREMENT = 1;
ALTER TABLE seckill_record     AUTO_INCREMENT = 1;

-- 11. 执行结果核对
SELECT 'sys_user' AS table_name, COUNT(*) AS rows_left FROM sys_user
UNION ALL SELECT 'user_token_account', COUNT(*) FROM user_token_account
UNION ALL SELECT 'mall_order', COUNT(*) FROM mall_order
UNION ALL SELECT 'mall_order_item', COUNT(*) FROM mall_order_item
UNION ALL SELECT 'order_status_log', COUNT(*) FROM order_status_log
UNION ALL SELECT 'payment_record', COUNT(*) FROM payment_record
UNION ALL SELECT 'seckill_record', COUNT(*) FROM seckill_record
UNION ALL SELECT 'token_transaction', COUNT(*) FROM token_transaction
UNION ALL SELECT 'user_token_plan', COUNT(*) FROM user_token_plan
UNION ALL SELECT 'token_usage_record', COUNT(*) FROM token_usage_record;

SELECT id, name, seckill_stock, sold_count, per_user_limit, status FROM seckill_activity;
SELECT id, sku_id, total_stock, available_stock, locked_stock FROM inventory ORDER BY id;
