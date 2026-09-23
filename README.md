# TokenMall 学习商城

TokenMall 是一个以 Redis、RabbitMQ 和 MySQL 为核心学习目标的模块化单体电商项目。

项目售卖两类数字商品：

- Token 资源包：支付成功后向用户账户发放 Token。
- Token Plan：具有有效期和 Token 配额的计划商品，其中部分 Plan 通过秒杀活动限量抢购。

## 学习目标

- 理解 Redis 在缓存、分布式锁、幂等、限流和秒杀预扣库存中的使用方式。
- 理解 RabbitMQ 在异步处理、可靠投递、重试、死信和延迟任务中的使用方式。
- 理解 MySQL 事务、索引、并发更新、锁、幂等表、状态机和最终一致性。
- 从可运行的简单版本出发，通过实验复现问题，再逐步实现优化。

## 当前阶段

项目骨架、基础 CRUD 和前后端页面已经跑通，目前按 14 天路线逐个复现并优化 MySQL、Redis、RabbitMQ 相关问题。

已包含：

- Spring Boot 3.3.5 + Java 17 后端：JWT 认证、商品与分类、购物车、订单、模拟支付、Token 账户、秒杀和管理端接口。
- React 18 + Vite + TypeScript 前端：用户端页面、管理端页面和接口联调。
- MySQL 表结构、种子数据和开发账号脚本。
- Redis 商品缓存、认证缓存和秒杀预扣练习代码。
- RabbitMQ 拓扑、生产者事件和本地运行脚本。
- JMeter 压测计划，用于复现秒杀和缓存问题。
- Obsidian 知识库、14 天教程和开发复盘。

当前仍在学习路线中：秒杀限购与库存一致性、缓存穿透与热点 Key、RabbitMQ 可靠投递与消费幂等。

## 目录说明

```text
.
├─ backend/               Spring Boot 后端
├─ frontend/              React + Vite 前端
├─ db/                    MySQL 初始化脚本
├─ docs/                  产品、架构、数据库、API、环境文档
├─ knowledge-vault/       Obsidian 知识库和 14 天教程
├─ perf/jmeter/           JMeter 压测计划
└─ scripts/               知识库索引和本地环境辅助脚本
```

## 快速开始

建议先阅读：

1. `docs/product/requirements.md`
2. `docs/product/roadmap-14-days.md`
3. `docs/product/prototype.md`
4. `docs/architecture/architecture.md`
5. `docs/operations/local-environment.md`
6. `docs/operations/rabbitmq-installation.md`
7. `perf/jmeter/README.md`

知识库入口：

- `knowledge-vault/00-Home/Home.md`

RabbitMQ 本地安装位置：

- `D:\DevTools\RabbitMQ`

启动和验证：

```powershell
.\scripts\rabbitmq\Start-RabbitMQ.ps1
.\scripts\rabbitmq\Test-RabbitMQ.ps1
```

本地开发服务：

最简单方式：双击桌面快捷方式 `TokenMall 一键启动`。

一键启动会启动 MySQL、RabbitMQ、Redis 和前端，不会启动后端。后端请在 IntelliJ IDEA 中运行或调试 `TokenMallApplication`。

也可以双击项目根目录：

- `启动TokenMall.cmd`
- `停止TokenMall.cmd`

命令行方式：

```powershell
.\scripts\dev\Start-Backend.ps1
.\scripts\dev\Start-Frontend.ps1
```

前端开发地址：

- `http://127.0.0.1:5173`

停止开发服务：

```powershell
.\scripts\dev\Stop-Backend.ps1
.\scripts\dev\Stop-Frontend.ps1
```

一键启动全部服务：

```powershell
.\scripts\dev\Start-All.ps1
```

一键停止辅助服务：

```powershell
.\scripts\dev\Stop-All.ps1
```

该命令会停止前端、Redis 和 RabbitMQ，不会停止 IDEA 管理的后端。

## 本地配置

后端连接信息集中在 `backend/src/main/resources/application.yml`，并支持环境变量覆盖，例如 `MYSQL_HOST`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PASSWORD`、`RABBITMQ_USERNAME`、`RABBITMQ_PASSWORD`、`JWT_SECRET`。

配置文件中的默认值是本地开发占位值，部署到其他环境前必须替换。

## 项目原则

- 先实现简单、能运行、存在明确问题的 MySQL 版本。
- 每个问题都有编号、复现方式和验收标准。
- Redis、RabbitMQ 和 MySQL 优化代码由学习者亲自完成。
- 前端由 AI 生成，作为验证后端接口和业务流程的工具。
- 知识库自动生成本次开发做了什么、验证了什么、还缺什么，并形成连续教程。
