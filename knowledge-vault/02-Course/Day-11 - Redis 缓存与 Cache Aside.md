---
type: course-day
day: 11
status: planned
tags:
  - tokenmall
  - course
  - redis
  - cache
concepts:
  - "[[Redis 缓存与并发]]"
code_paths:
  - backend/src/main/java/com/tokenmall/catalog
  - backend/src/main/java/com/tokenmall/common/redis
related:
  - "[[Day-10 - 延迟队列、超时关闭和 Outbox]]"
  - "[[Day-12 - 缓存一致性与异常场景]]"
---

# Day 11：Redis 缓存与 Cache Aside

## 今日目标

- 使用 Spring Data Redis 连接本机 Redis 3.0.504。
- 使用 Cache Aside 缓存商品详情和 Plan。
- 设计 Key、序列化和 TTL。
- 观察缓存命中前后的 SQL 次数和响应时间。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习 Redis 数据结构和 Spring Data Redis |
| 上午二 | 接入 Redis 客户端和序列化 |
| 下午一 | 实现商品详情 Cache Aside |
| 下午二 | 对比缓存前后查询和耗时 |
| 晚上 | 整理缓存策略，提交 `day11` |

## Cache Aside 流程

### 查询

```text
读取缓存
  ├─ 命中 -> 返回
  └─ 未命中 -> 查询 MySQL -> 写入缓存 -> 返回
```

### 更新

推荐先更新数据库，再删除缓存。

不要在每次更新时直接写入缓存，除非能保证并发顺序。

## Key 设计

```text
mall:product:detail:{productId}
mall:sku:detail:{skuId}
mall:seckill:activity:{activityId}
```

要求：

- Key 前缀统一。
- 不包含用户隐私。
- TTL 明确。
- 序列化格式统一。
- Key 版本变更时可加 `v1`。

## TTL 建议

| 数据 | TTL |
| --- | --- |
| 商品详情 | 10 至 30 分钟 |
| SKU 详情 | 10 至 30 分钟 |
| 秒杀活动元数据 | 1 至 5 分钟 |
| 用户 Token 账户 | 30 至 60 秒，第一版可暂不缓存 |

TTL 起始值不是标准答案，需要用实验调整。

## 序列化

建议使用 JSON，不要直接使用 Java 原生序列化。

原因：

- 可读。
- 跨语言兼容。
- 升级对象时更容易处理。
- 避免 JDK 序列化安全问题。

## 代码入口

```text
backend/src/main/java/com/tokenmall/common/redis/RedisConfig
backend/src/main/java/com/tokenmall/catalog/service/ProductQueryService
backend/src/main/java/com/tokenmall/catalog/cache/ProductCacheService
```

## 实践任务

- [ ] 配置 Redis 连接。
- [ ] 封装统一的缓存 Key 工具。
- [ ] 缓存商品详情 DTO，而不是数据库 Entity。
- [ ] 缓存 Token Plan 的配额和有效期信息。
- [ ] 查询命中时不再执行商品详情 SQL。
- [ ] 商品修改后删除缓存。
- [ ] 观察 Redis Key 和 TTL。
- [ ] 记录缓存前后响应时间。

## 验证证据

需要保存：

- Redis CLI 的 `GET` 或图形客户端截图说明。
- SQL 日志中第一次和第二次查询的差异。
- 缓存命中时的应用日志。
- 更新商品后缓存被删除的日志。

## 实验

### 实验 1：首次和第二次查询

首次查询应回源 MySQL，第二次查询应命中缓存。

### 实验 2：修改后查询

修改商品后删除缓存，再次查询应回源并重建。

### 实验 3：手工删除 Key

删除缓存后观察查询自动恢复。

## 验收标准

- [ ] Redis 成功连接。
- [ ] 商品详情支持缓存。
- [ ] 缓存值不包含敏感数据。
- [ ] TTL 和 Key 命名符合规范。
- [ ] 能对比缓存前后数据库查询次数。
- [ ] 能说明 Cache Aside 的一致性问题。

## 常见错误

- 缓存 Entity，导致字段升级不兼容。
- Key 没有业务前缀。
- 缓存永久不过期。
- 返回了缓存数据但忘记处理空值。
- 更新数据库后忘记删除缓存。

## 面试问题

- 为什么常用 Cache Aside？
- 先删缓存还是先更新数据库？
- 缓存对象和缓存 ID 各有什么优缺点？
- 为什么不要把缓存当成权威数据源？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-10 - 延迟队列、超时关闭和 Outbox]]
- 下一天：[[Day-12 - 缓存一致性与异常场景]]
- 概念：[[Redis 缓存与并发]]
