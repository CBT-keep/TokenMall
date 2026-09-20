---
type: course-day
day: 8
status: planned
tags:
  - tokenmall
  - course
  - rabbitmq
  - retry
  - dlq
concepts:
  - "[[RabbitMQ 可靠消息]]"
code_paths:
  - backend/src/main/java/com/tokenmall/messaging/config
  - backend/src/main/java/com/tokenmall/messaging/consumer
related:
  - "[[Day-07 - 异步支付和 Token 发放]]"
  - "[[Day-09 - 消费幂等与重复投递]]"
---

# Day 08：ACK、重试和死信

## 今日目标

- 理解自动 ACK、手动 ACK、NACK 和 requeue。
- 实现有限次数重试。
- 把超过重试次数的消息路由到死信队列。
- 能够从 DLQ 查看、分析和重放消息。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习 ACK、NACK 和 requeue |
| 上午二 | 实现异常消息消费者 |
| 下午一 | 配置重试和 DLQ |
| 下午二 | 验证重放流程 |
| 晚上 | 记录消息状态图，提交 `day08` |

## 状态变化

```text
Ready
  -> Consumer 接收
  -> Unacked
  -> ACK -> 消息删除
  -> NACK(requeue=false) -> DLX
  -> Consumer 断开 -> Ready
```

## 重试原则

- 瞬时故障可以重试。
- 参数错误和业务永久失败不应无限重试。
- 重试必须有次数上限。
- 重试需要退避，避免重试风暴。
- DLQ 不是垃圾桶，需要告警和人工处理。

## 推荐重试策略

```text
第 1 次失败：等待 1 秒
第 2 次失败：等待 3 秒
第 3 次失败：等待 9 秒
超过 3 次：进入 DLQ
```

也可以在 RabbitMQ 中通过 TTL + DLX 实现延迟重试。

## 代码入口

```text
backend/src/main/java/com/tokenmall/messaging/config/RabbitMQConfig
backend/src/main/java/com/tokenmall/messaging/consumer/TokenGrantConsumer
backend/src/main/java/com/tokenmall/messaging/consumer/DeadLetterConsumer
backend/src/main/resources/application.yml
```

## 实践任务

- [ ] 消费者只使用手动 ACK。
- [ ] 业务成功后显式 ACK。
- [ ] 可重试异常抛出并进入重试。
- [ ] 永久异常直接投递到 DLQ。
- [ ] 配置 `mall.token.grant.dlq`。
- [ ] 发送一条必然失败的消息。
- [ ] 在 Management UI 查看重试和死信。
- [ ] 修复原因后重放 DLQ 消息。

## 必须观察的指标

- Ready 数量。
- Unacked 数量。
- 重试次数。
- DLQ 数量。
- 消费者日志中的 `eventId`。
- 消息首次发生时间和最终处理时间。

## 实验

### 实验 1：消费者直接崩溃

在处理中强制退出进程，确认消息重新入队。

### 实验 2：业务异常

让 Token 账户查询抛异常，观察重试。

### 实验 3：永久错误

制造不存在的订单，确认进入 DLQ。

### 实验 4：重放

修复订单数据后重新发布 DLQ 消息，确认可以成功处理。

## 验收标准

- [ ] 消息不会因为异常静默丢失。
- [ ] 重试次数有限。
- [ ] 失败消息进入 DLQ。
- [ ] 能根据 `eventId` 搜索日志。
- [ ] 能解释何时应该 requeue，何时不应该。
- [ ] 死信重放后业务结果正确。

## 常见错误

- 异常时无限 requeue。
- 使用自动 ACK。
- 消费成功但 ACK 失败，导致重复投递。
- DLQ 没有消费者，问题无人发现。
- 捕获异常后直接返回，导致消息被错误 ACK。

## 面试问题

- ACK 丢失会发生什么？
- 为什么无限重试会放大故障？
- 死信队列和延迟队列有什么区别？
- 如何避免重试消息挤占正常消息？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-07 - 异步支付和 Token 发放]]
- 下一天：[[Day-09 - 消费幂等与重复投递]]
- 概念：[[RabbitMQ 可靠消息]]
