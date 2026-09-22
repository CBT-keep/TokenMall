# 登录认证缓存设计

## 目标

避免每个携带 JWT 的业务请求都查询 `sys_user`。

原来的链路：

```text
JWT -> username -> MySQL 查询 sys_user -> SecurityContext
```

新的链路：

```text
JWT -> userId
  -> Caffeine 本地缓存
  -> Redis
  -> MySQL
  -> 回填 Redis 和本地缓存
```

## JWT 内容

新 Token 只保存最小身份信息：

```json
{
  "sub": "1",
  "uid": 1,
  "iat": 178...,
  "exp": 178...
}
```

密码、用户名和角色不再放入 JWT。`uid` 保留是为了兼容旧 Token。

## 缓存数据

```json
{
  "userId": 1,
  "username": "admin",
  "role": "ADMIN",
  "enabled": true
}
```

Redis Key：

```text
mall:user:auth:{userId}
```

示例：

```text
mall:user:auth:1
```

## 配置

```yaml
tokenmall:
  auth-cache:
    local-ttl-seconds: 300
    redis-ttl-seconds: 1800
    local-maximum-size: 10000
```

环境变量：

```text
AUTH_LOCAL_CACHE_TTL_SECONDS
AUTH_REDIS_CACHE_TTL_SECONDS
AUTH_LOCAL_CACHE_MAX_SIZE
```

## 回源规则

1. 本地缓存命中：直接返回。
2. 本地缓存未命中，Redis 命中：写入本地缓存后返回。
3. Redis 也未命中：查询 MySQL。
4. MySQL 查询成功：写入 Redis 和本地缓存。
5. MySQL 用户不存在：返回 401。

Redis 不可用时不会阻断业务，会自动退化为本地缓存和 MySQL。

## 登录流程

1. 按用户名查询一次 MySQL。
2. 校验 BCrypt 密码。
3. 校验用户状态。
4. 写入 Caffeine 和 Redis。
5. 生成只包含用户 ID 的 JWT。

登录本身不参与缓存命中，因为必须验证密码。

## 缓存失效

用户状态、角色或认证信息变化时，必须调用：

```java
userAuthCacheService.evict(userId);
```

当前项目还没有管理员修改用户状态和角色的接口。增加该接口时，需要同时调用缓存失效。

## 一致性与风险

- 本地缓存 TTL 最长时，单实例内可能短暂使用旧权限。
- 多实例部署时，本地缓存不会自动同步，应迁移到 Redis 或使用消息广播失效。
- Redis 数据不是权威数据，MySQL 仍然是权威数据源。
- JWT 的过期时间仍然有效，本方案暂未实现 Token 版本号或黑名单。

## 验证结果

已验证：

- 登录后 Caffeine 写入成功。
- 连续请求命中本地缓存，不再查询 `sys_user`。
- 重启后端后，本地缓存为空，第一次请求命中 Redis。
- Redis 命中请求仍然没有查询 `sys_user`。
- Redis Key TTL 为 1800 秒。
