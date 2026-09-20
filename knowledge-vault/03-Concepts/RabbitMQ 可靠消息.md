---
type: concept
status: active
tags:
  - tokenmall
  - rabbitmq
  - messaging
---

# RabbitMQ 可靠消息

## 本概念解决的问题

- 支付成功后如何异步发放 Token。
- 消费者异常时消息如何重试。
- 重复投递如何保证业务只执行一次。
- 超时订单如何延迟关闭。
- 数据库事务和消息发送如何保持一致。

## 核心知识

- Exchange、Queue、Binding、Routing Key。
- Publisher Confirm。
- 手动 ACK 和 NACK。
- requeue、重试和 DLQ。
- 消息幂等。
- TTL + DLX。
- Outbox。

## 关联课程

- [[Day-06 - RabbitMQ 基础与拓扑]]
- [[Day-07 - 异步支付和 Token 发放]]
- [[Day-08 - ACK、重试和死信]]
- [[Day-09 - 消费幂等与重复投递]]
- [[Day-10 - 延迟队列、超时关闭和 Outbox]]
- [[Day-14 - 秒杀综合实现、对账与复盘]]

## 关联实验

- [[消息重复投递实验]]
