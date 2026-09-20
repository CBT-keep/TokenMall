---
type: course-day
day: 10
status: planned
tags:
  - tokenmall
  - course
  - rabbitmq
  - delay
  - outbox
concepts:
  - "[[RabbitMQ 可靠消息]]"
  - "[[MySQL 事务与锁]]"
code_paths:
  - backend/src/main/java/com/tokenmall/order
  - backend/src/main/java/com/tokenmall/messaging
  - db/01_schema.sql
related:
  - "[[Day-09 - 消费幂等与重复投递]]"
  - "[[Day-11 - Redis 缓存与 Cache Aside]]"
---

# Day 10：延迟队列、超时关闭和 Outbox

## 今日目标

- 使用 RabbitMQ 替代数据库轮询关闭超时订单。
- 理解 TTL + DLX 实现延迟消息。
- 处理支付和关闭订单的并发竞争。
- 理解 Outbox 如何解决数据库和 MQ 的不一致。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习 TTL、DLX 和延迟队列 |
| 上午二 | 配置超时消息拓扑 |
| 下午一 | 实现超时关闭消费者 |
| 下午二 | 实现 Outbox 基础流程 |
| 晚上 | 验证并发场景，提交 `day10` |

## 延迟消息方案

```text
创建订单
  -> 发布到 mall.delay.exchange
  -> mall.order.timeout.delay.queue
  -> TTL 到期
  -> DLX 到 mall.dlx.exchange
  -> mall.order.timeout.queue
  -> 超时关闭消费者
```

RabbitMQ 有延迟消息插件，但本项目优先学习 TTL + DLX，理解消息过期和死信机制。

## 关键约束：队头阻塞

如果多个延迟时间不同的消息进入同一队列，先到期的消息可能被前面的长延迟消息阻塞。

可选方案：

- 每种延迟时长使用独立队列。
- 使用延迟消息插件。
- 使用时间轮或调度服务。

14 天项目统一使用 15 分钟订单超时，避免队列头阻塞。

## 超时关闭事务

```text
开始事务
  -> 条件更新订单：待支付 -> 已关闭
  -> 如果更新成功，恢复库存
  -> 写状态日志
提交事务
```

条件更新：

```sql
UPDATE mall_order
SET status = 'CLOSED'
WHERE order_no = #{orderNo}
  AND status = 'PENDING_PAYMENT';
```

如果受影响行数为 0：

- 订单可能已经支付。
- 订单可能已取消。
- 消息可能重复。

以上情况都不能恢复库存。

## 实验：支付与关闭竞争

让支付请求和超时消息几乎同时到达。

预期结果：

- 只有一个操作可以从 `PENDING_PAYMENT` 成功。
- 支付成功则不能关闭。
- 关闭成功则不能支付。
- 库存恢复最多执行一次。

## Outbox 学习

### 问题

```text
数据库事务提交成功
MQ 发送失败
```

订单已支付，但 Token 永远不发放。

### 方案

在同一数据库事务中写入 `message_outbox`：

```text
更新订单
插入 outbox 事件
提交事务
定时任务扫描 NEW 事件
发送到 RabbitMQ
成功后标记 SENT
```

这不是绝对可靠，但把“本地事务”和“待发送消息”放在同一个事务中。

## 代码入口

```text
backend/src/main/java/com/tokenmall/order/service/OrderTimeoutService
backend/src/main/java/com/tokenmall/messaging/config/DelayQueueConfig
backend/src/main/java/com/tokenmall/messaging/outbox
backend/src/main/resources/mapper/MessageOutboxMapper.xml
```

## 实践任务

- [ ] 创建订单时发送超时消息。
- [ ] 延迟消息通过 DLX 进入关闭队列。
- [ ] 消费者条件关闭订单。
- [ ] 关闭成功时恢复库存。
- [ ] 重复消息不会重复恢复库存。
- [ ] 支付和关闭并发时只有一个成功。
- [ ] 支付成功时在同一事务写 Outbox。
- [ ] 扫描任务发布 Outbox 并更新状态。

## 验收标准

- [ ] 不再使用固定时间全表扫描关闭订单。
- [ ] 超时订单在预期时间内关闭。
- [ ] 已支付订单不会被关闭。
- [ ] 库存恢复幂等。
- [ ] 发送失败的 Outbox 可以重试。
- [ ] 能说明 Outbox 与直接发送消息的区别。

## 常见错误

- 先关闭订单，再检查是否已支付。
- 关闭失败也恢复库存。
- 延迟队列混用不同超时时间。
- Outbox 扫描任务并发执行导致重复发送。
- 发送成功但更新 Outbox 失败，导致重复发送。

## 面试问题

- RabbitMQ TTL 队列为什么可能出现队头阻塞？
- 支付和超时关闭如何保证互斥？
- Outbox 如何配合消费幂等？
- 定时轮询和延迟消息各有什么优缺点？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-09 - 消费幂等与重复投递]]
- 下一天：[[Day-11 - Redis 缓存与 Cache Aside]]
- 概念：[[RabbitMQ 可靠消息]]
