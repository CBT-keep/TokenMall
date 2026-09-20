# TokenMall Frontend

## 运行

```powershell
pnpm install
pnpm dev
```

或使用项目脚本：

```powershell
..\scripts\dev\Start-Frontend.ps1
```

默认地址：

```text
http://127.0.0.1:5173
```

Vite 会把 `/api` 代理到：

```text
http://localhost:8080
```

## 构建

```powershell
pnpm build
```

## 页面

用户端：

- 登录和注册
- 首页、商品列表和商品详情
- 购物车和结算
- 订单列表和详情
- 模拟支付
- Token 账户、Plan 和流水
- 秒杀

管理端：

- 控制台
- 分类管理
- 商品管理
- SKU 与库存
- 订单管理
- 秒杀活动管理
