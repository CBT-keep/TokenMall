---
type: course-day
day: 12
status: planned
tags:
  - tokenmall
  - course
  - redis
  - cache
  - consistency
concepts:
  - "[[Redis 缓存与并发]]"
code_paths:
  - backend/src/main/java/com/tokenmall/catalog/cache
  - backend/src/main/java/com/tokenmall/messaging/consumer/CacheInvalidationConsumer
related:
  - "[[Day-11 - Redis 缓存与 Cache Aside]]"
  - "[[Day-13 - 分布式锁、Lua、幂等与限流]]"
  - "[[缓存失效实验]]"
---

# Day 12：缓存一致性与异常场景

## 今日目标

- 复现缓存与数据库不一致。
- 处理缓存穿透、击穿和雪崩。
- 使用 MQ 传递缓存失效事件。
- 区分一致性、可用性和性能之间的问题。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习四类缓存问题 |
| 上午二 | 实现空值缓存和 TTL 抖动 |
| 下午一 | 复现热点 Key 击穿 |
| 下午二 | 实现互斥重建和 MQ 失效 |
| 晚上 | 对比实验结果，提交 `day12` |

## 1. 缓存穿透

现象：

- 请求不存在的商品。
- 缓存永远不命中。
- 每次请求都访问 MySQL。

方案：

- 缓存空值，TTL 较短。
- 布隆过滤器，Redis 3.0 可以使用 `SETBIT` 手工实验。
- 接口参数和业务规则提前拒绝。

## 2. 缓存击穿

现象：

- 单个热点 Key 过期。
- 大量并发请求同时回源数据库。

方案：

- 互斥锁重建。
- 热点数据逻辑过期。
- 热点 Key 不过期，后台刷新。

## 3. 缓存雪崩

现象：

- 大量 Key 同时过期。
- Redis 短暂不可用。
- 数据库瞬时流量过大。

方案：

- TTL 添加随机抖动。
- 多级缓存。
- 限流和降级。
- Redis 高可用。

## 4. 缓存一致性

常见顺序：

```text
方案 A：先删除缓存，再更新数据库
方案 B：先更新数据库，再删除缓存
```

项目采用方案 B，并分析：

- 并发读在旧值写入缓存后的窗口。
- 删除缓存失败。
- 主从延迟。
- 延迟双删。
- 通过 MQ 异步删除缓存。

## 代码入口

```text
backend/src/main/java/com/tokenmall/catalog/cache/CacheKey
backend/src/main/java/com/tokenmall/catalog/cache/ProductCacheService
backend/src/main/java/com/tokenmall/catalog/cache/EmptyValuePolicy
backend/src/main/java/com/tokenmall/catalog/cache/HotKeyRebuildLock
backend/src/main/java/com/tokenmall/messaging/consumer/CacheInvalidationConsumer
```

## 实践任务

- [ ] 请求不存在的商品并记录 SQL 次数。
- [ ] 缓存空值并验证第二次请求不回源。
- [ ] 为商品 Key 加入随机 TTL。
- [ ] 让热点 Key 过期并并发回源。
- [ ] 使用锁或互斥重建限制回源数量。
- [ ] 商品更新后发布缓存失效事件。
- [ ] 消费者删除商品缓存。
- [ ] 记录缓存删除失败时如何重试。

## 一致性实验矩阵

| 实验 | 操作顺序 | 预期问题 |
| --- | --- | --- |
| A | 更新数据库后立即查询 | 可能读到旧缓存 |
| B | 更新数据库后删除缓存 | 并发窗口仍可能写回旧值 |
| C | 先删缓存后更新数据库 | 并发查询可能重建旧值 |
| D | MQ 异步删除 | 短暂不一致，但最终一致 |

## 验收标准

- [ ] 能复现缓存穿透。
- [ ] 能复现热点 Key 击穿。
- [ ] 能构造批量同时过期。
- [ ] 能展示一次旧值读取。
- [ ] 能通过删除缓存恢复一致。
- [ ] 能说明缓存一致性不能简单依靠一个顺序解决。

## 常见错误

- 缓存空值 TTL 很长，导致商品上架后仍返回空。
- 锁粒度太大，导致所有商品查询串行。
- TTL 随机范围过小。
- 删除缓存失败后没有重试。
- 将 MQ 异步删除理解为强一致。

## 面试问题

- 缓存穿透、击穿、雪崩有什么区别？
- 为什么缓存更新通常选择删除而不是修改？
- 延迟双删真的能保证一致性吗？
- 缓存删除失败如何处理？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-11 - Redis 缓存与 Cache Aside]]
- 下一天：[[Day-13 - 分布式锁、Lua、幂等与限流]]
- 实验：[[缓存失效实验]]
