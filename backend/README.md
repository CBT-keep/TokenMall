# TokenMall Backend

## 运行

前提：

- 已执行 `db/01_schema.sql`、`db/02_seed.sql` 和 `db/03_create_dev_user.sql`。
- MySQL、Redis、RabbitMQ 已启动。

```powershell
mvn spring-boot:run
```

或使用项目脚本：

```powershell
..\scripts\dev\Start-Backend.ps1
```

默认地址：

```text
http://localhost:8080
```

## 编译

```powershell
mvn -DskipTests package
```

## 故意保留的学习基线

- 普通库存使用查询后更新，可能超卖。
- 秒杀直接查改 MySQL，不经过 Redis。
- 支付成功后同步发放 Token，不经过 RabbitMQ。
- 消费幂等、重试、死信和延迟任务尚未实现。

代码中使用 `LEARNING-BASELINE`、`LEARNING-TODO` 和 `LEARNING-VERIFY` 标记这些位置。
