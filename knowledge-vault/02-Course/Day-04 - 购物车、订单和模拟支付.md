---
type: course-day
day: 4
status: planned
tags:
  - tokenmall
  - course
  - order
  - payment
concepts:
  - "[[MySQL 事务与锁]]"
code_paths:
  - backend/src/main/java/com/tokenmall/cart
  - backend/src/main/java/com/tokenmall/order
  - backend/src/main/java/com/tokenmall/payment
  - backend/src/main/java/com/tokenmall/token
related:
  - "[[Day-03 - Spring Security 与 JWT]]"
  - "[[Day-05 - MySQL 并发与超卖]]"
---

# Day 04：购物车、订单和模拟支付

## 今日目标

- 跑通普通商品购买流程。
- 理解订单状态机和事务边界。
- 理解订单快照、模拟支付和 Token 发放。
- 主动发现简单实现中的一致性问题。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习订单状态机和购物车模型 |
| 上午二 | 阅读订单创建、支付和 Token 发放代码 |
| 下午一 | 从前端完成一次完整购买 |
| 下午二 | 复现重复支付和事务异常 |
| 晚上 | 记录基线问题，提交 `day04` |

## 完整流程

```text
商品详情
  -> 加入购物车
  -> 选择购物车项
  -> 创建订单
  -> 扣库存
  -> 模拟支付
  -> 更新订单
  -> 发放 Token 或创建 Plan
  -> 查看 Token 账户
```

## 核心概念

- 订单状态机。
- 订单快照。
- 总金额和实付金额。
- 事务边界。
- 幂等请求 ID。
- 重复支付。
- 模拟支付回调。
- Token 流水。
- 用户 Plan。

## 代码入口

```text
backend/src/main/java/com/tokenmall/cart
backend/src/main/java/com/tokenmall/order
backend/src/main/java/com/tokenmall/payment
backend/src/main/java/com/tokenmall/token
```

重点查看：

- `OrderService.createOrder`
- `OrderService.cancelOrder`
- `PaymentService.mockSuccess`
- `TokenService.grantForOrder`
- `UserTokenPlan`
- `TokenTransaction`

## 实践任务

- [ ] 加入一个 Token 资源包到购物车。
- [ ] 修改数量并结算。
- [ ] 查看创建出的订单和订单项。
- [ ] 检查库存是否减少。
- [ ] 调用模拟支付成功。
- [ ] 检查订单状态、支付记录、Token 余额和流水。
- [ ] 直接购买一个 Token Plan，检查用户 Plan。
- [ ] 取消一个待支付订单，检查库存恢复。

## 必须主动复现的问题

### 问题 1：重复支付

对同一订单连续调用两次模拟支付成功，记录 Token 是否发放两次。

### 问题 2：事务异常

在订单创建后人为抛出异常，检查库存、订单和订单项是否同时回滚。

### 问题 3：库存与订单不一致

减少库存后制造订单插入失败，检查库存是否恢复。

### 问题 4：重复下单

使用相同 `requestId` 连续提交两次，检查订单数量。

## 验收标准

- [ ] 完整购买流程能走通。
- [ ] 订单状态变化符合状态机。
- [ ] 资源包和 Plan 发放结果正确。
- [ ] 能够指出至少四个一致性风险。
- [ ] 能说明哪些问题留给 Redis、MQ 或 MySQL 阶段解决。

## 常见错误

- Controller 手动拼装订单号。
- 订单和订单项不在同一事务。
- 支付成功后先发 Token 再更新订单。
- 取消订单重复恢复库存。
- 使用商品当前价格渲染历史订单。

## 面试问题

- 订单为什么不能直接依赖商品表实时数据？
- 支付回调为什么必须幂等？
- 订单关闭和支付同时发生怎么办？
- 哪些逻辑适合同步执行，哪些适合 MQ 异步执行？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-03 - Spring Security 与 JWT]]
- 下一天：[[Day-05 - MySQL 并发与超卖]]
- 概念：[[MySQL 事务与锁]]
