---
type: course-day
day: 1
status: planned
tags:
  - tokenmall
  - course
  - environment
concepts:
  - "[[MySQL 事务与锁]]"
code_paths:
  - db/01_schema.sql
  - db/02_seed.sql
  - docs/architecture/architecture.md
related:
  - "[[Day-02 - SQL、模块与简单 CRUD]]"
---

# Day 01：环境、项目与领域认知

## 今日目标

- 理解 TokenMall 为什么采用模块化单体。
- 明确 Token 资源包和 Token Plan 的差异。
- 启动 MySQL、Redis 和 RabbitMQ。
- 执行数据库脚本。
- 启动后端和前端简单版本，走通一次登录、商品查询和订单流程。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 阅读需求、架构和数据库设计，画业务对象关系 |
| 上午二 | 启动 MySQL、Redis、RabbitMQ，执行 SQL |
| 下午一 | 启动后端和前端，调用登录、商品和订单接口 |
| 下午二 | 阅读请求链路和模块代码，记录代码入口 |
| 晚上 | 更新日志，提交 `day01` |

## 核心概念

- 模块化单体：一个部署单元，多个业务模块。
- 最终一致数据源：MySQL 是权威数据源。
- 数字商品：没有物流和收货地址。
- 资源包：一次性到账 Token。
- Token Plan：有有效期和配额，可能参与秒杀。
- 简单基线：先让业务能运行，不提前实现 Redis 和 MQ 优化。

## 业务对象

需要能画出以下关系：

```text
用户 -> 购物车 -> 订单 -> 支付 -> Token 账户
商品 -> SKU -> 库存
秒杀活动 -> Token Plan SKU -> 秒杀记录 -> 订单
```

## 代码入口

后端代码在项目骨架生成后位于：

```text
backend/src/main/java/com/tokenmall
```

重点查看：

- 启动类。
- `common` 的统一响应和异常处理。
- `auth` 的登录入口。
- `catalog` 的商品查询。
- `order` 的创建订单。
- `payment` 的模拟支付。

数据库脚本：

- `db/01_schema.sql`
- `db/02_seed.sql`
- `db/03_create_dev_user.sql`

## 实践任务

- [ ] 启动 MySQL 服务。
- [ ] 使用管理员账号执行 `db/01_schema.sql`。
- [ ] 执行 `db/02_seed.sql`。
- [ ] 执行 `db/03_create_dev_user.sql`。
- [ ] 使用 `token_mall_dev` 登录数据库并查询商品和库存。
- [ ] 启动 Redis，执行 `PING`。
- [ ] 检查 RabbitMQ 服务和 Management UI。
- [ ] 启动后端，确认日志没有连接和编码错误。
- [ ] 启动前端，使用 `admin/admin123` 登录。
- [ ] 调用商品列表、创建订单和模拟支付接口。

## 数据库检查 SQL

```sql
USE token_mall;

SELECT id, username, role, status FROM sys_user;
SELECT id, product_type, name, price, status FROM product ORDER BY sort_order;
SELECT sku_id, total_stock, available_stock, locked_stock FROM inventory;
SELECT id, name, seckill_price, seckill_stock, sold_count, status
FROM seckill_activity;
```

## 实验

### 实验 1：确认初始库存

记录 SKU `1006` 的初始库存。

### 实验 2：确认管理员角色

使用普通用户 Token 调用管理端接口，预期返回 403。

### 实验 3：确认简单基线

创建一个普通订单并模拟支付，确认 Token 账户发生变化。

## 验收标准

- [ ] 四个基础设施组件状态明确。
- [ ] 三个数据库脚本执行成功。
- [ ] 管理员可以登录。
- [ ] 前端能加载商品和秒杀活动。
- [ ] 创建订单和模拟支付可以完成。
- [ ] 能解释 MySQL、Redis、RabbitMQ 各自在项目中的职责。

## 常见错误

- MySQL 服务未启动。
- `db/03_create_dev_user.sql` 使用了错误的管理员账号。
- Redis 命令没有加入 PATH，需要从安装目录执行。
- RabbitMQ 的 Erlang 版本不兼容。
- Windows 控制台编码导致中文乱码。

## 面试问题

- 为什么这个项目不一开始拆微服务？
- MySQL、Redis 和 RabbitMQ 各自保存什么？
- 数字商品订单和实物订单有什么区别？
- 为什么秒杀不能复用普通购物车下单流程？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 下一天：[[Day-02 - SQL、模块与简单 CRUD]]
- 项目需求：`docs/product/requirements.md`
- 项目架构：`docs/architecture/architecture.md`
