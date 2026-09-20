---
type: course-day
day: 9
status: planned
tags:
  - tokenmall
  - course
  - rabbitmq
  - idempotency
  - mysql
concepts:
  - "[[RabbitMQ 可靠消息]]"
  - "[[MySQL 事务与锁]]"
code_paths:
  - backend/src/main/java/com/tokenmall/messaging/consumer
  - backend/src/main/java/com/tokenmall/token
  - db/01_schema.sql
related:
  - "[[Day-08 - ACK、重试和死信]]"
  - "[[Day-10 - 延迟队列、超时关闭和 Outbox]]"
  - "[[消息重复投递实验]]"
---

# Day 09：消费幂等与重复投递

## 今日目标

- 证明 RabbitMQ 至少一次投递会产生重复消息。
- 使用 `mq_consume_log` 防止重复消费。
- 使用业务唯一键作为第二层保护。
- 区分消费幂等和接口幂等。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习至少一次投递和幂等 |
| 上午二 | 实现消费日志 |
| 下午一 | 制造重复投递并验证 |
| 下午二 | 实现业务唯一键保护 |
| 晚上 | 整理幂等矩阵，提交 `day09` |

## 为什么会重复

- 消费者处理成功后 ACK 丢失。
- 网络断开导致消息重新入队。
- 生产者重试发送同一事件。
- 运维手工重放 DLQ。
- Broker 或消费者重启。

结论：

```text
至少一次投递 + 幂等消费 = 业务只成功一次
```

## 两层幂等

### 第一层：消费日志

```text
consumer_group + event_id -> 唯一索引
```

处理前尝试插入：

- 插入成功：继续处理。
- 唯一键冲突：已经处理过，直接 ACK。

### 第二层：业务唯一键

Token 发放可以使用：

```text
idempotency_key = eventId + ":" + orderItemId + ":" + productType
```

`token_transaction.uk_idempotency_key` 保证重复发放失败。

## 实现顺序

1. 接收事件。
2. 校验事件信封。
3. 在独立事务中插入消费日志。
4. 执行业务逻辑。
5. 业务成功后将消费日志更新为 `SUCCESS`。
6. ACK 消息。
7. 重复消息直接跳过并 ACK。

## 代码入口

```text
backend/src/main/java/com/tokenmall/messaging/consumer/MqConsumeLogService
backend/src/main/java/com/tokenmall/token/service/TokenGrantService
backend/src/main/resources/mapper/MqConsumeLogMapper.xml
```

## 实践任务

- [ ] 定义 `consumer_group`。
- [ ] 使用 `eventId` 插入 `mq_consume_log`。
- [ ] 捕获 MySQL 唯一键冲突。
- [ ] 实现业务重复判断。
- [ ] 记录原始消息和错误信息。
- [ ] 重复发送同一事件两次。
- [ ] 验证余额只增加一次。
- [ ] 验证重复消息仍然 ACK。

## 状态机

```text
PROCESSING
  ├─ 成功 -> SUCCESS
  └─ 永久失败 -> FAILED
```

注意：

- 插入 `PROCESSING` 后进程崩溃。
- 消息重新投递时会遇到已有记录。
- 需要设计超时接管或允许重新处理。

## 实验

### 实验 1：相同 `eventId`

发布两次完全相同的事件，检查 Token 余额。

### 实验 2：不同 `eventId`，相同业务含义

模拟生产者重复生成新 ID。验证业务唯一键是否仍能防重。

### 实验 3：处理成功后不 ACK

让消费者完成后强制断开，确认消息重投但业务不重复。

## 验收标准

- [ ] 重复事件不会重复增加余额。
- [ ] 消费日志有明确状态。
- [ ] 能解释两个不同 `eventId` 为什么危险。
- [ ] 重复消息最终被 ACK，不会无限堆积。
- [ ] 发现并记录处理中崩溃的边界问题。

## 常见错误

- 先查再插，存在并发窗口。
- 依赖内存 Set 做幂等。
- 消费日志和业务操作不在同一事务，但顺序设计错误。
- 重复消息抛异常后无限重试。
- 只使用 `eventId`，不检查业务唯一键。

## 面试问题

- 为什么消费幂等不能只靠 Redis？
- 消费日志插入成功但业务失败怎么办？
- 幂等键应该如何设计？
- 至少一次、至多一次和恰好一次有什么区别？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-08 - ACK、重试和死信]]
- 下一天：[[Day-10 - 延迟队列、超时关闭和 Outbox]]
- 实验：[[消息重复投递实验]]
