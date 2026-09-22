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

当前仓库处于项目规划阶段，已经包含：

- 产品需求和验收标准。
- 14 天学习与开发路线。
- 模块化单体架构设计。
- 数据库设计和可执行 SQL 脚本。
- REST API 契约。
- Obsidian 知识库与教程框架。
- RabbitMQ 本地安装和运行脚本。

需求和知识库已经确认，下一步按 Day 01 至 Day 14 搭建后端与前端基础代码。

## 目录说明

```text
.
├─ db/                    MySQL 初始化脚本
├─ docs/                  产品、架构、数据库、API、环境文档
├─ knowledge-vault/       Obsidian 知识库和 14 天教程
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

一键停止应用服务：

```powershell
.\scripts\dev\Stop-All.ps1
```

## 项目原则

- 先实现简单、能运行、存在明确问题的 MySQL 版本。
- 每个问题都有编号、复现方式和验收标准。
- Redis、RabbitMQ 和 MySQL 优化代码由学习者亲自完成。
- 前端由 AI 生成，作为验证后端接口和业务流程的工具。
- 知识库自动生成本次开发做了什么、验证了什么、还缺什么，并形成连续教程。
