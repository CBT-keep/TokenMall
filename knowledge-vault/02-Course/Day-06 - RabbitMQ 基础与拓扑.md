---
type: course-day
day: 6
status: planned
tags:
  - tokenmall
  - course
  - rabbitmq
concepts:
  - "[[RabbitMQ 可靠消息]]"
code_paths:
  - backend/src/main/java/com/tokenmall/messaging
  - scripts/rabbitmq
related:
  - "[[Day-05 - MySQL 并发与超卖]]"
  - "[[Day-07 - 异步支付和 Token 发放]]"
---

# Day 06：RabbitMQ 基础与拓扑

## 今日目标

- 理解 AMQP 核心对象。
- 启动本地 RabbitMQ 并进入 Management UI。
- 建立项目需要的 Exchange、Queue 和 Binding。
- 编写第一个生产者和消费者。
- 观察 ACK 前后队列和消息状态。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习交换器、队列、绑定和路由键 |
| 上午二 | 启动 RabbitMQ，浏览管理台 |
| 下午一 | 建立基础拓扑并发送消息 |
| 下午二 | 实现第一个消费者和手动 ACK |
| 晚上 | 画消息拓扑，提交 `day06` |

## AMQP 核心对象

| 对象 | 作用 |
| --- | --- |
| Producer | 发送消息 |
| Exchange | 接收消息并路由 |
| Binding | 描述 Exchange 和 Queue 的绑定关系 |
| Routing Key | 路由条件 |
| Queue | 保存消息 |
| Consumer | 消费消息 |
| Channel | 在连接上复用的会话 |

## 交换器类型

- Direct：精确匹配 Routing Key。
- Topic：按模式匹配。
- Fanout：广播到所有绑定队列。
- Headers：按消息头匹配，本项目不重点使用。

## 项目拓扑

```text
mall.order.exchange
  order.paid -> mall.token.grant.queue
  order.created -> mall.order.created.queue
  product.changed -> mall.cache.invalidation.queue

mall.delay.exchange
  order.timeout -> mall.order.timeout.delay.queue

mall.dlx.exchange
  order.timeout -> mall.order.timeout.queue
  token.grant.failed -> mall.token.grant.dlq
```

## Management UI 检查项

- Connections。
- Channels。
- Exchanges。
- Queues。
- Bindings。
- Message rates。
- Ready 和 Unacked 数量。

## 代码入口

```text
backend/src/main/java/com/tokenmall/messaging/config
backend/src/main/java/com/tokenmall/messaging/event
backend/src/main/java/com/tokenmall/messaging/producer
backend/src/main/java/com/tokenmall/messaging/consumer
```

## 实践任务

- [ ] 启动 RabbitMQ，访问 `http://localhost:15672`。
- [ ] 使用 `guest/guest` 登录。
- [ ] 创建 `mall.order.exchange`。
- [ ] 创建 `mall.token.grant.queue`。
- [ ] 使用 Routing Key `order.paid` 绑定。
- [ ] 在 Spring Boot 中配置连接。
- [ ] 发送一条 `order.paid` 测试事件。
- [ ] 在管理台查看队列和消息。
- [ ] 消费者手动 ACK 后确认消息消失。

## 手动 ACK 实验

在消费者中：

1. 先接收消息但不 ACK。
2. 查看 Unacked 数量。
3. 关闭消费者。
4. 观察消息重新回到 Ready。
5. 正常 ACK 后再观察。

## 验收标准

- [ ] 能解释 Exchange 和 Queue 的区别。
- [ ] 能解释 Binding 和 Routing Key 的关系。
- [ ] 能从管理台定位消息。
- [ ] Spring Boot 能发送和消费消息。
- [ ] 能解释 Ready 和 Unacked。
- [ ] 能画出项目目标拓扑。

## 常见错误

- Exchange 名称或 Routing Key 不一致。
- 队列不存在时消息被丢弃。
- 忘记声明 Binding。
- 消费者自动 ACK，异常时消息丢失。
- 在代码中硬编码账号密码。

## 面试问题

- 未绑定队列的 Exchange 会怎样处理消息？
- 自动 ACK 和手动 ACK 有何差异？
- 消息投递是推还是拉？
- 为什么 RabbitMQ 不适合长期堆积海量事件？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-05 - MySQL 并发与超卖]]
- 下一天：[[Day-07 - 异步支付和 Token 发放]]
- 概念：[[RabbitMQ 可靠消息]]
