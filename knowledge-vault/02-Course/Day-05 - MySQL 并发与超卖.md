---
type: course-day
day: 5
status: planned
tags:
  - tokenmall
  - course
  - mysql
  - concurrency
concepts:
  - "[[MySQL 事务与锁]]"
code_paths:
  - backend/src/main/java/com/tokenmall/inventory
  - backend/src/main/java/com/tokenmall/order
  - db/01_schema.sql
related:
  - "[[Day-04 - 购物车、订单和模拟支付]]"
  - "[[Day-06 - RabbitMQ 基础与拓扑]]"
  - "[[并发扣库存实验]]"
---

# Day 05：MySQL 并发与超卖

## 今日目标

- 使用并发请求稳定复现库存超卖。
- 理解“先查询、再判断、再更新”的竞态。
- 使用 MySQL 行锁和条件更新修复超卖。
- 理解索引、事务和锁之间的联动。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习事务隔离、行锁和当前读 |
| 上午二 | 编写并发复现脚本 |
| 下午一 | 复现超卖并记录数据 |
| 下午二 | 实现两种 MySQL 修复方案 |
| 晚上 | 对比结果，提交 `day05` |

## 初始问题代码形态

```java
Inventory inventory = inventoryMapper.selectBySkuId(skuId);
if (inventory.getAvailableStock() < quantity) {
    throw new BusinessException("库存不足");
}
inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
inventoryMapper.updateById(inventory);
```

问题在于查询和更新之间没有原子性。

## 核心概念

- 读写竞态。
- 丢失更新。
- 当前读和快照读。
- `SELECT ... FOR UPDATE`。
- 条件更新。
- 乐观锁版本号。
- 死锁和锁等待。
- `EXPLAIN`。

## 实验准备

把 SKU `1001` 调整成较小库存，例如 10。

并发发送 50 个购买请求，每个购买 1 件。统计：

- 成功请求数。
- 失败请求数。
- 最终可用库存。
- 成功订单数量。
- 超卖数量。

## 建议实验命令

PowerShell 可以使用 `ForEach-Object -Parallel` 或单独脚本并发调用接口。实验前先备份库存数据。

## 修复方案一：条件更新

```sql
UPDATE inventory
SET available_stock = available_stock - #{quantity}
WHERE sku_id = #{skuId}
  AND available_stock >= #{quantity};
```

检查受影响行数：

- 返回 1 表示扣减成功。
- 返回 0 表示库存不足或状态不符合。

该方案简单，适合普通下单第一阶段。

## 修复方案二：行锁

```sql
SELECT *
FROM inventory
WHERE sku_id = #{skuId}
FOR UPDATE;
```

然后在同一事务中判断和更新。

需要观察：

- 锁等待时间。
- 并发请求是否串行化。
- 长事务对吞吐量的影响。

## 深度实验

### 实验 1：相同 SKU

所有请求竞争同一行库存，观察锁等待。

### 实验 2：不同 SKU

请求分散到不同 SKU，对比吞吐量。

### 实验 3：制造死锁

在一个事务中按不同顺序更新两个 SKU，观察 MySQL 死锁日志。

### 实验 4：无索引更新

使用没有索引的条件更新，观察锁范围变化。

## 验收标准

- [ ] 初始版本可以稳定复现超卖。
- [ ] 成功后 `初始库存 - 成功数量 = 剩余库存`。
- [ ] 能解释 `SELECT FOR UPDATE` 为什么有效。
- [ ] 能解释条件更新为什么不需要先查询。
- [ ] 能使用 `EXPLAIN` 说明索引使用情况。
- [ ] 记录修复前后成功数量和耗时。

## MySQL 修复的局限

即使 MySQL 不超卖：

- 所有请求仍然直接到达数据库。
- 秒杀高并发下数据库会成为瓶颈。
- 锁等待会造成请求阻塞。
- 数据库修复只能作为最终防线，不能替代 Redis 预扣。

## 面试问题

- 为什么 `库存 > 0` 判断后更新会超卖？
- 乐观锁和悲观锁分别适合什么场景？
- 条件更新受影响行数为 0 有哪些可能？
- InnoDB 行锁在什么情况下会升级或扩大？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-04 - 购物车、订单和模拟支付]]
- 下一天：[[Day-06 - RabbitMQ 基础与拓扑]]
- 实验：[[并发扣库存实验]]
