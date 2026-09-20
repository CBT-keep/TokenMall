---
type: course-day
day: 3
status: planned
tags:
  - tokenmall
  - course
  - security
  - jwt
concepts:
  - "[[MySQL 事务与锁]]"
code_paths:
  - backend/src/main/java/com/tokenmall/auth
  - backend/src/main/java/com/tokenmall/common/security
related:
  - "[[Day-02 - SQL、模块与简单 CRUD]]"
  - "[[Day-04 - 购物车、订单和模拟支付]]"
---

# Day 03：Spring Security 与 JWT

## 今日目标

- 理解 Spring Security Filter Chain。
- 理解用户名密码认证和 JWT 认证的区别。
- 完成注册、登录、当前用户和角色鉴权。
- 在前端完成 Token 保存、自动携带和 401 处理。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 学习认证、授权和 JWT 结构 |
| 上午二 | 阅读 Security 配置和 JWT 过滤器 |
| 下午一 | 验证注册、登录、权限和异常响应 |
| 下午二 | 在前端接入登录态和路由保护 |
| 晚上 | 整理安全问题，提交 `day03` |

## 核心概念

- `Authentication` 和 `Authorization`。
- `SecurityFilterChain`。
- `UserDetailsService`。
- `PasswordEncoder`。
- Bearer Token。
- JWT 的 Header、Payload 和 Signature。
- `ROLE_USER` 和 `ROLE_ADMIN`。
- 无状态 Session。

## 代码入口

```text
backend/src/main/java/com/tokenmall/auth
backend/src/main/java/com/tokenmall/common/security
backend/src/main/resources/application.yml
```

重点查看：

- `SecurityConfig`
- `JwtAuthenticationFilter`
- `JwtTokenProvider`
- `AuthController`
- `AuthService`
- `CustomUserDetailsService`

## 实践任务

- [ ] 注册一个普通用户。
- [ ] 登录并复制 JWT。
- [ ] 解析 JWT Payload，查看用户 ID、用户名、角色和过期时间。
- [ ] 调用 `/api/v1/auth/me`。
- [ ] 不带 Token 调用受保护接口，确认返回 401。
- [ ] 使用普通用户调用管理端接口，确认返回 403。
- [ ] 使用管理员调用管理端接口，确认成功。
- [ ] 前端退出后清理 Token 并跳转登录页。

## 安全检查

必须确认：

- 数据库不保存明文密码。
- JWT 密钥来自环境变量。
- Token 过期后不能继续访问。
- 前端不能根据本地角色字段绕过后端鉴权。
- 401 表示未认证，403 表示无权限。

## 实验

### 实验 1：修改签名

手工修改 JWT 最后一个字符，请求应返回 401。

### 实验 2：过期 Token

把过期时间设置为 10 秒，等待后再次请求。

### 实验 3：角色越权

普通用户请求：

```http
GET /api/v1/admin/products
```

记录 Security 日志和响应。

## 验收标准

- [ ] 注册和登录成功。
- [ ] 密码为 BCrypt。
- [ ] JWT 能正确签发和验证。
- [ ] USER 和 ADMIN 权限隔离。
- [ ] 前端可以自动附加 Token。
- [ ] 401 和 403 前端表现正确。

## 常见错误

- `ROLE_ADMIN` 和 `ADMIN` 前缀混用。
- JWT 过滤器执行两次。
- 把密钥提交到 Git。
- 登录失败统一返回 500。
- 前端只用路由守卫，不依赖后端鉴权。

## 面试问题

- JWT 如何防止被篡改？
- JWT 能主动失效吗？
- Session 和 JWT 的差异是什么？
- Spring Security 的过滤器链如何工作？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-02 - SQL、模块与简单 CRUD]]
- 下一天：[[Day-04 - 购物车、订单和模拟支付]]
