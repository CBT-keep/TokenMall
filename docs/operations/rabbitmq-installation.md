# RabbitMQ 本地安装记录

## 1. 选择结果

本机没有 Docker Desktop 和 WSL2，因此采用 Windows 原生便携版：

| 组件 | 版本 |
| --- | --- |
| RabbitMQ | 4.3.6 |
| Erlang/OTP | 28.5 |
| 架构 | 64 位 Windows |

安装目录固定在 D 盘：

```text
D:\DevTools\RabbitMQ
├─ downloads                  官方 ZIP 缓存
├─ erlang-28.5                Erlang 运行时
├─ rabbitmq-server-4.3.6      RabbitMQ 服务端
├─ data                       Mnesia、Cookie、插件配置
└─ logs                       日志
```

## 2. 为什么选择这个组合

RabbitMQ 官方兼容性说明中，RabbitMQ 4.3.6 支持 Erlang/OTP 27 和 28。选择 OTP 28.5 是为了：

- 处于官方支持范围。
- 避免使用过旧的 OTP 26。
- 比刚发布的 OTP 29 更稳妥。
- 与当前 RabbitMQ 4.3.6 匹配。

## 3. 下载来源

Erlang：

```text
https://github.com/erlang/otp/releases/download/OTP-28.5/otp_win64_28.5.zip
```

RabbitMQ：

```text
https://github.com/rabbitmq/rabbitmq-server/releases/download/v4.3.6/rabbitmq-server-windows-4.3.6.zip
```

如果 GitHub 直连较慢，可以使用只做 HTTP 转发的代理地址。脚本只负责下载，不修改系统服务。

## 4. 目录环境变量

项目脚本会为当前进程设置：

```text
ERLANG_HOME=D:\DevTools\RabbitMQ\erlang-28.5
RABBITMQ_BASE=D:\DevTools\RabbitMQ\data
RABBITMQ_LOG_BASE=D:\DevTools\RabbitMQ\logs
RABBITMQ_MNESIA_BASE=D:\DevTools\RabbitMQ\data\mnesia
```

这样可以避免 RabbitMQ 默认把数据写到用户目录。

## 5. 启动与验证

启动：

```powershell
.\scripts\rabbitmq\Start-RabbitMQ.ps1
```

启用管理插件：

```powershell
.\scripts\rabbitmq\Enable-Management.ps1
```

查看状态：

```powershell
.\scripts\rabbitmq\Status-RabbitMQ.ps1
```

完整测试：

```powershell
.\scripts\rabbitmq\Test-RabbitMQ.ps1
```

停止：

```powershell
.\scripts\rabbitmq\Stop-RabbitMQ.ps1
```

## 6. 管理界面

```text
http://localhost:15672
```

本地学习默认账号：

```text
guest / guest
```

项目专用虚拟主机和账号：

```text
vhost: tokenmall
username: tokenmall_dev
password: ChangeMe_123456
```

初始化命令：

```powershell
.\scripts\rabbitmq\Initialize-RabbitMQ.ps1
```

## 7. 验证项

- [ ] `erl -version` 可以执行。
- [ ] `rabbitmqctl status` 成功。
- [ ] 5672 端口监听。
- [ ] 15672 端口监听。
- [ ] 管理界面可以登录。
- [ ] `tokenmall` 虚拟主机存在。
- [ ] `tokenmall_dev` 可以访问该虚拟主机。
- [ ] 数据和日志位于 D 盘。

## 8. 常见问题

### Erlang 版本不匹配

确认：

```powershell
erl -version
```

如果 PATH 中同时存在多个 Erlang，以 `ERLANG_HOME` 和脚本设置的 PATH 为准。

### 节点名称冲突

如果以前安装过 RabbitMQ，旧节点数据可能使用同一节点名。

解决方式：

- 使用独立 `RABBITMQ_BASE`。
- 为当前环境设置独立节点名。
- 不要直接删除旧节点数据。

### Cookie 不一致

`rabbitmqctl` 和 RabbitMQ 服务端必须读取同一个 Erlang Cookie。

项目脚本会把 `HOME`、`HOMEDRIVE` 和 `HOMEPATH` 指向 D 盘数据目录，确保两个命令使用一致环境。

### 15672 无法访问

执行：

```powershell
.\scripts\rabbitmq\Enable-Management.ps1
.\scripts\rabbitmq\Status-RabbitMQ.ps1
```

启用插件后可能需要重启 RabbitMQ。
