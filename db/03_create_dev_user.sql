-- Run this script with a MySQL administrator account.
-- Change the password before using it outside a local learning environment.

CREATE USER IF NOT EXISTS 'token_mall_dev'@'localhost'
IDENTIFIED BY 'ChangeMe_123456';

ALTER USER 'token_mall_dev'@'localhost'
IDENTIFIED BY 'ChangeMe_123456';

GRANT ALL PRIVILEGES ON token_mall.* TO 'token_mall_dev'@'localhost';

FLUSH PRIVILEGES;

-- Application environment variables:
-- MYSQL_DATABASE=token_mall
-- MYSQL_USERNAME=token_mall_dev
-- MYSQL_PASSWORD=ChangeMe_123456
