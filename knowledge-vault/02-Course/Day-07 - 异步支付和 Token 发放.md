---
type: course-day
day: 7
status: planned
tags:
  - tokenmall
  - course
  - rabbitmq
  - payment
concepts:
  - "[[RabbitMQ 可靠消息]]"
code_paths:
  - backend/src/main/java/com/tokenmall/payment
  - backend/src/main/java/com/tokenmall/token
  - backend/src/main/java/com/tokenmall/messaging
related:
  - "[[Day-06 - RabbitMQ 基础与拓扑]]"
  - "[[Day-08 - ACK、重试和死信]]"
---

# Day 07：异步支付和 Token 发放

## 今日目标

- 把支付成功后的 Token 发放改成 MQ 异步处理。
- 定义稳定的事件模型。
- 理解消息发送与数据库事务不一致。
- 观察异步处理后接口响应和最终结果。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 设计 `order.paid` 事件 |
| 上午二 | 实现事件发布和消费者骨架 |
| 下午一 | 把 Token 发放迁移到消费者 |
| 下午二 | 验证资源包和 Plan 两条路径 |
| 晚上 | 复现一致性问题，提交 `day07` |

## 目标链路

```text
模拟支付成功
  -> 更新支付记录
  -> 更新订单状态
  -> 发布 order.paid
  -> 消费者接收
  -> 发放资源包 Token 或创建 Plan
  -> 写 Token 流水
```

## 事件模型

```json
{
  "eventId": "uuid",
  "eventType": "order.paid",
  "eventVersion": 1,
  "occurredAt": "2026-09-20T12:00:00+08:00",
  "aggregateType": "ORDER",
  "aggregateId": "order-no",
  "traceId": "trace-id",
  "payload": {
    "orderNo": "order-no",
    "userId": 2,
    "payAmount": 9.90,
    "items": [
      {
        "productType": "TOKEN_PACK",
        "skuId": 1001,
        "quantity": 1,
        "tokenAmount": 1000,
        "planDays": null,
        "planQuota": null
      }
    ]
  }
}
```

## 代码入口

```text
backend/src/main/java/com/tokenmall/payment/service
backend/src/main/java/com/tokenmall/messaging/event/OrderPaidEvent
backend/src/main/java/com/tokenmall/messaging/producer/OrderEventProducer
backend/src/main/java/com/tokenmall/messaging/consumer/TokenGrantConsumer
backend/src/main/java/com/tokenmall/token/service
```

## 实践任务

- [ ] 定义事件信封和 `OrderPaidEvent`。
- [ ] 支付成功后发布事件。
- [ ] 消费者反序列化事件。
- [ ] 删除控制器中同步发放 Token 的调用。
- [ ] 消费者处理资源包商品。
- [ ] 消费者处理 Token Plan 商品。
- [ ] 前端轮询订单或 Token 账户查看结果。
- [ ] 记录支付接口耗时变化。

## 必须复现的问题

### 问题 1：事务回滚但消息已发送

在订单事务提交前发送消息，然后制造后续异常。

结果：

- 数据库回滚。
- MQ 已收到支付成功事件。
- 消费者可能给不存在或未支付的订单发放 Token。

### 问题 2：事务提交但消息发送失败

订单已支付，但 RabbitMQ 连接失败。

结果：

- 订单状态正确。
- Token 永远没有发放。

### 问题 3：消费者处理失败

Token 发放时抛出异常。

先记录当前行为，Day 08 再处理重试和 DLQ。

## Broker Confirm 学习点

开启 Publisher Confirm 后：

- Broker 确认接收表示消息到达交换器。
- Routing Key 没有匹配队列时仍可能被确认。
- Confirm 不等于消费者已经处理成功。
- 需要配合 Return Callback 和消费端幂等。

## 验收标准

- [ ] 支付和 Token 发放已经解耦。
- [ ] 资源包和 Plan 都能异步发放。
- [ ] 事件字段完整且可追踪。
- [ ] 能复现至少一种数据库和 MQ 不一致。
- [ ] 能说明最终一致性不是强一致性。

## 常见错误

- 消息只传订单号，消费者无法获得商品快照。
- 事件字段命名随意，版本升级后无法兼容。
- 使用数据库 Entity 作为事件对象。
- 消息发送失败只打印日志。
- 支付接口直接返回 Token 已到账，但消费者还没有完成。

## 面试问题

- 为什么支付成功后不直接同步发放 Token？
- 消息发送成功是否等于业务成功？
- 如何保证支付和消息的一致性？
- 事件应该携带实体 ID 还是完整快照？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-06 - RabbitMQ 基础与拓扑]]
- 下一天：[[Day-08 - ACK、重试和死信]]
- 实验：[[消息重复投递实验]]
