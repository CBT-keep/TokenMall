# TokenMall REST API 契约

## 1. 基础约定

基础路径：

```text
/api/v1
```

请求和响应格式：

```text
application/json
```

认证方式：

```http
Authorization: Bearer <jwt>
```

分页参数：

```text
page=1&size=10
```

分页响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "records": [],
    "page": 1,
    "size": 10,
    "total": 0
  },
  "traceId": "trace-id"
}
```

普通响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "traceId": "trace-id"
}
```

错误响应：

```json
{
  "code": 40001,
  "message": "库存不足",
  "data": null,
  "traceId": "trace-id"
}
```

## 2. 错误码

| 错误码 | HTTP 状态 | 含义 |
| --- | --- | --- |
| 0 | 200 | 成功 |
| 40001 | 400 | 参数错误 |
| 40002 | 400 | 库存不足 |
| 40003 | 400 | 商品不可购买 |
| 40004 | 400 | 订单状态不允许操作 |
| 40005 | 400 | 秒杀未开始或已结束 |
| 40006 | 400 | 超过购买限制 |
| 40101 | 401 | 未登录或 Token 无效 |
| 40301 | 403 | 无权限 |
| 40401 | 404 | 资源不存在 |
| 40901 | 409 | 重复请求 |
| 50001 | 500 | 系统内部错误 |
| 50002 | 500 | 消息处理失败 |
| 50003 | 500 | 对账发现状态不一致 |

## 3. 认证接口

### 3.1 注册

```http
POST /api/v1/auth/register
```

请求：

```json
{
  "username": "learner",
  "password": "learner123",
  "nickname": "学习者"
}
```

响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "userId": 2,
    "username": "learner",
    "nickname": "学习者",
    "role": "USER"
  }
}
```

### 3.2 登录

```http
POST /api/v1/auth/login
```

请求：

```json
{
  "username": "learner",
  "password": "learner123"
}
```

响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 2,
      "username": "learner",
      "nickname": "学习者",
      "role": "USER"
    }
  }
}
```

### 3.3 当前用户

```http
GET /api/v1/auth/me
```

## 4. 公开商品接口

### 4.1 分类列表

```http
GET /api/v1/categories
```

### 4.2 商品列表

```http
GET /api/v1/products?type=TOKEN_PACK&page=1&size=10
```

`type` 可选值：

- `TOKEN_PACK`
- `TOKEN_PLAN`

### 4.3 商品详情

```http
GET /api/v1/products/{productId}
```

响应数据需要包含商品、SKU 和库存摘要：

```json
{
  "id": 1,
  "name": "1,000 Token 资源包",
  "productType": "TOKEN_PACK",
  "description": "一次性到账的 Token 资源包",
  "price": 9.90,
  "tokenAmount": 1000,
  "planDays": null,
  "planQuota": null,
  "status": 1,
  "skus": [
    {
      "id": 1,
      "skuCode": "TP-1000",
      "name": "1,000 Tokens",
      "price": 9.90,
      "availableStock": 10000,
      "tokenAmount": 1000
    }
  ]
}
```

## 5. 购物车接口

### 5.1 查看购物车

```http
GET /api/v1/cart
```

### 5.2 加入购物车

```http
POST /api/v1/cart/items
```

请求：

```json
{
  "skuId": 1,
  "quantity": 1
}
```

### 5.3 修改数量

```http
PUT /api/v1/cart/items/{cartItemId}
```

请求：

```json
{
  "quantity": 2,
  "selected": true
}
```

### 5.4 删除购物车项

```http
DELETE /api/v1/cart/items/{cartItemId}
```

## 6. 订单接口

### 6.1 从购物车创建订单

```http
POST /api/v1/orders
```

请求：

```json
{
  "cartItemIds": [1, 2],
  "requestId": "client-generated-uuid"
}
```

### 6.2 直接购买

```http
POST /api/v1/orders/direct
```

请求：

```json
{
  "skuId": 1,
  "quantity": 1,
  "requestId": "client-generated-uuid"
}
```

### 6.3 订单列表

```http
GET /api/v1/orders?status=PENDING_PAYMENT&page=1&size=10
```

### 6.4 订单详情

```http
GET /api/v1/orders/{orderNo}
```

### 6.5 取消订单

```http
POST /api/v1/orders/{orderNo}/cancel
```

## 7. 模拟支付接口

### 7.1 查询支付状态

```http
GET /api/v1/payments/{orderNo}
```

### 7.2 模拟支付成功

```http
POST /api/v1/payments/mock/success
```

请求：

```json
{
  "orderNo": "202609200001",
  "requestId": "client-generated-uuid"
}
```

重复调用必须返回相同业务结果，不能重复发放 Token。

## 8. Token 接口

### 8.1 Token 账户

```http
GET /api/v1/token/account
```

响应：

```json
{
  "packBalance": 5000,
  "planBalance": 200000,
  "totalPurchased": 205000,
  "totalConsumed": 0
}
```

### 8.2 用户 Plan 列表

```http
GET /api/v1/token/plans
```

### 8.3 Token 流水

```http
GET /api/v1/token/transactions?page=1&size=20
```

### 8.4 消费 Token，可选

```http
POST /api/v1/token/consume
```

请求：

```json
{
  "amount": 100,
  "requestId": "client-generated-uuid",
  "description": "调用模型接口"
}
```

## 9. 秒杀接口

### 9.1 秒杀活动列表

```http
GET /api/v1/seckill/activities
```

### 9.2 秒杀活动详情

```http
GET /api/v1/seckill/activities/{activityId}
```

### 9.3 发起秒杀

```http
POST /api/v1/seckill/activities/{activityId}/orders
```

请求：

```json
{
  "requestId": "client-generated-uuid",
  "quantity": 1
}
```

初始响应：

```json
{
  "success": true,
  "orderNo": "202609200002",
  "message": "抢购成功，请尽快支付"
}
```

Redis 和 MQ 版本可以返回：

```json
{
  "accepted": true,
  "requestId": "client-generated-uuid",
  "message": "请求已受理，请稍后查询结果"
}
```

### 9.4 查询秒杀结果

```http
GET /api/v1/seckill/requests/{requestId}
```

## 10. 管理端接口

所有接口要求 `ROLE_ADMIN`。

### 10.1 仪表盘

```http
GET /api/v1/admin/dashboard
```

### 10.2 分类管理

```http
GET    /api/v1/admin/categories
POST   /api/v1/admin/categories
PUT    /api/v1/admin/categories/{id}
DELETE /api/v1/admin/categories/{id}
```

### 10.3 商品管理

```http
GET    /api/v1/admin/products
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}
```

### 10.4 SKU 与库存管理

```http
GET /api/v1/admin/skus
POST /api/v1/admin/skus
PUT /api/v1/admin/skus/{id}
PUT /api/v1/admin/inventory/{skuId}/adjust
```

### 10.5 订单管理

```http
GET /api/v1/admin/orders
GET /api/v1/admin/orders/{orderNo}
```

### 10.6 秒杀活动管理

```http
GET    /api/v1/admin/seckill/activities
POST   /api/v1/admin/seckill/activities
PUT    /api/v1/admin/seckill/activities/{id}
DELETE /api/v1/admin/seckill/activities/{id}
GET    /api/v1/admin/seckill/activities/{id}/records
```

## 11. 接口实现顺序

1. 认证和用户。
2. 分类、商品和 SKU。
3. 库存。
4. 购物车。
5. 订单。
6. 模拟支付。
7. Token 账户和 Plan。
8. 秒杀活动。
9. 管理端聚合接口。
10. Redis 和 MQ 优化接口保持原路径不变，只改变内部实现。

## 12. 前端对接约束

- 前端不关心后端用 MySQL、Redis 还是 MQ 实现。
- 接口响应结构保持稳定。
- 秒杀接口允许从同步结果升级为异步受理。
- 所有创建和支付接口必须支持 `requestId`。
- 时间统一使用 ISO 8601 字符串。
- 金额使用数字，前端展示时保留两位小数。
