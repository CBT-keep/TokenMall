---
type: concept
status: active
tags:
  - tokenmall
  - mysql
  - transaction
---

# MySQL 事务与锁

## 本概念解决的问题

- 并发扣库存为什么可能超卖。
- 事务回滚后哪些数据会恢复。
- 行锁、唯一索引和乐观锁分别适合什么场景。
- 为什么先查询再更新不是一个原子操作。

## 核心知识

- ACID。
- 隔离级别。
- InnoDB 行锁和间隙锁。
- `SELECT ... FOR UPDATE`。
- 条件更新和版本号。
- 唯一索引幂等。
- `EXPLAIN`。

## 关联课程

- [[Day-02 - SQL、模块与简单 CRUD]]
- [[Day-04 - 购物车、订单和模拟支付]]
- [[Day-05 - MySQL 并发与超卖]]
- [[Day-09 - 消费幂等与重复投递]]
- [[Day-14 - 秒杀综合实现、对账与复盘]]

## 关联实验

- [[并发扣库存实验]]
- [[秒杀对账实验]]
