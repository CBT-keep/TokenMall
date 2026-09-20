---
type: course-day
day: 13
status: planned
tags:
  - tokenmall
  - course
  - redis
  - lock
  - lua
  - rate-limit
concepts:
  - "[[Redis 缓存与并发]]"
  - "[[秒杀系统]]"
code_paths:
  - backend/src/main/java/com/tokenmall/common/redis/lock
  - backend/src/main/java/com/tokenmall/common/redis/lua
  - backend/src/main/java/com/tokenmall/seckill
related:
  - "[[Day-12 - 缓存一致性与异常场景]]"
  - "[[Day-14 - 秒杀综合实现、对账与复盘]]"
---

# Day 13：分布式锁、Lua、幂等与限流

## 今日目标

- 使用 `SET NX PX` 实现锁。
- 使用唯一值避免误删别人的锁。
- 使用 Lua 原子释放锁。
- 使用 Lua 实现库存判断、扣减和限流。
- 理解锁续期和锁失效边界。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习分布式锁语义 |
| 上午二 | 实现加锁和 Lua 解锁 |
| 下午一 | 实现 Lua 扣库存和限流 |
| 下午二 | 制造锁过期和并发实验 |
| 晚上 | 整理 Lua 脚本，提交 `day13` |

## 基础锁

```text
SET lock:key unique-value NX PX 30000
```

含义：

- Key 不存在才设置。
- 设置成功表示获得锁。
- 30 秒后自动过期。
- Value 用于证明锁的所属者。

## 解锁必须使用 Lua

错误方式：

```text
GET lock:key
比较 value
DEL lock:key
```

比较和删除之间可能锁已过期，并被其他线程重新获取。

Lua 解锁：

```lua
if redis.call("GET", KEYS[1]) == ARGV[1] then
  return redis.call("DEL", KEYS[1])
else
  return 0
end
```

## 锁续期

问题：

- 业务执行超过 30 秒。
- 锁过期。
- 两个线程同时进入临界区。

方案：

- 合理设置超时。
- 看门狗自动续期。
- 业务上增加数据库最终防线。

第一版需要先手工复现锁失效，再决定是否引入续期。

## Lua 库存扣减

```lua
local stock = tonumber(redis.call("GET", KEYS[1]))
if stock == nil then
  return -1
end
if stock < tonumber(ARGV[1]) then
  return 0
end
redis.call("DECRBY", KEYS[1], ARGV[1])
return 1
```

返回语义：

- `-1`：库存未初始化。
- `0`：库存不足。
- `1`：扣减成功。

生产脚本需要进一步处理用户限购、幂等和补偿记录。

## 限流

可以实践：

- 固定窗口计数。
- 滑动窗口近似。
- 令牌桶。

简化 Lua 固定窗口：

```lua
local count = redis.call("INCR", KEYS[1])
if count == 1 then
  redis.call("EXPIRE", KEYS[1], ARGV[1])
end
if count > tonumber(ARGV[2]) then
  return 0
end
return 1
```

## 幂等 Token

流程：

1. 客户端申请请求 ID。
2. 服务端使用 Redis 抢占请求 ID。
3. 抢占成功继续执行。
4. 抢占失败读取已保存结果或返回处理中。

需要分析：

- Redis 设置成功但业务失败。
- Redis 过期但数据库事务仍运行。
- Redis 和数据库幂等记录不一致。

## 代码入口

```text
backend/src/main/java/com/tokenmall/common/redis/lock/RedisLock
backend/src/main/java/com/tokenmall/common/redis/lock/RedisLockManager
backend/src/main/java/com/tokenmall/common/redis/lua/LuaScripts
backend/src/main/java/com/tokenmall/common/ratelimit/RedisRateLimiter
backend/src/main/java/com/tokenmall/seckill/service/SeckillStockService
```

## 实践任务

- [ ] 实现加锁、解锁和锁所有权校验。
- [ ] 制造锁过期后业务继续执行。
- [ ] 验证 Lua 解锁不会删除别人的锁。
- [ ] 加载 Lua 库存扣减脚本。
- [ ] 并发调用脚本并验证不会扣成负数。
- [ ] 实现用户级限流。
- [ ] 实现秒杀请求 ID 幂等。
- [ ] 记录每个方案的失败窗口。

## 验收标准

- [ ] 能解释为什么解锁必须校验 Value。
- [ ] 能解释 Redis 锁不是绝对可靠。
- [ ] Lua 扣库存不会产生负数。
- [ ] 限流阈值可验证。
- [ ] 同一请求只处理一次。
- [ ] 能说出锁、Lua 和数据库唯一键如何配合。

## 常见错误

- 使用 `SETNX` 和独立 `EXPIRE`，两步之间可能崩溃。
- 锁 Value 使用固定字符串。
- 解锁不校验所有权。
- 认为 Redis 锁可以完全替代数据库约束。
- Lua 脚本中逻辑过多，难以调试。

## 面试问题

- Redis 分布式锁有哪些典型问题？
- 锁过期后如何避免两个线程同时执行？
- Lua 为什么能保证多步 Redis 命令原子性？
- 限流和幂等有什么区别？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-12 - 缓存一致性与异常场景]]
- 下一天：[[Day-14 - 秒杀综合实现、对账与复盘]]
- 概念：[[Redis 缓存与并发]]
- 概念：[[秒杀系统]]
