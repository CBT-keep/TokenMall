---
type: concept
status: active
tags:
  - tokenmall
  - redis
  - cache
---

# Redis 缓存与并发

## 本概念解决的问题

- 如何减少商品详情查询数据库的次数。
- 缓存和数据库怎样保持一致。
- 热点 Key 失效后如何避免数据库被打穿。
- 分布式锁如何保护临界区。
- 秒杀如何预扣库存。

## 核心知识

- String、Hash、List、Set、ZSet。
- Cache Aside。
- TTL、随机抖动、空值缓存、布隆过滤器。
- `SET NX PX`、Lua、锁续期。
- 限流、幂等、原子扣减。
- Redis 3.0.504 的功能边界。

## 关联课程

- [[Day-11 - Redis 缓存与 Cache Aside]]
- [[Day-12 - 缓存一致性与异常场景]]
- [[Day-13 - 分布式锁、Lua、幂等与限流]]
- [[Day-14 - 秒杀综合实现、对账与复盘]]

## 关联实验

- [[缓存失效实验]]
- [[秒杀对账实验]]
