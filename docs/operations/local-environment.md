# TokenMall 本地开发环境

## 1. 已确认环境

| 组件 | 当前情况 |
| --- | --- |
| 操作系统 | Windows 11 |
| Java | Temurin JDK 17.0.18 |
| Maven | 3.9.14 |
| MySQL | 8.0.34 |
| Redis | 3.0.504 |
| RabbitMQ | 4.3.6，已部署到 `D:\DevTools\RabbitMQ` |
| Erlang | OTP 28.5，已部署到 `D:\DevTools\RabbitMQ` |
| Node.js | 24.13.0 |
| npm | 11.6.2 |
| pnpm | 11.9.0 |
| Git | 已安装 |
| Obsidian | `D:\obsidian\Obsidian.com` |

## 2. 固定目录约定

RabbitMQ 和 Erlang 统一部署到：

```text
D:\DevTools\RabbitMQ
```

计划目录：

```text
D:\DevTools\RabbitMQ
├─ downloads
├─ erlang-28.5
├─ rabbitmq-server-4.3.6
├─ data
└─ logs
```

项目目录：

```text
E:\new_project
```

知识库：

```text
E:\new_project\knowledge-vault
```

## 3. 端口约定

| 服务 | 端口 |
| --- | --- |
| Spring Boot | 8080 |
| React 开发服务器 | 5173 |
| MySQL | 3306 |
| Redis | 6379 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management UI | 15672 |

## 4. MySQL

当前 MySQL 服务名：

```text
MySQL
```

检查服务：

```powershell
Get-Service MySQL
```

启动服务：

```powershell
Start-Service MySQL
```

执行脚本：

```powershell
& 'D:\MySQL\mysql-8.0.34-winx64\mysql-8.0.34-winx64\bin\mysql.exe' -u root -p
```

进入 MySQL 后执行：

```sql
SOURCE E:/new_project/db/01_schema.sql;
SOURCE E:/new_project/db/02_seed.sql;
SOURCE E:/new_project/db/03_create_dev_user.sql;
```

数据库操作由项目学习者执行。

## 5. Redis

Redis 安装目录：

```text
D:\Redis-x64-3.0.504
```

启动：

```powershell
Set-Location 'D:\Redis-x64-3.0.504'
.\redis-server.exe .\redis.windows.conf
```

连接：

```powershell
Set-Location 'D:\Redis-x64-3.0.504'
.\redis-cli.exe
```

验证：

```redis
PING
SET tokenmall:health ok
GET tokenmall:health
```

注意：

- 该版本较旧，只用于本地学习。
- 不开放公网访问。
- 不使用 Redis Streams 和 Redis Functions。

## 6. RabbitMQ

安装完成后使用以下目录：

```text
D:\DevTools\RabbitMQ\erlang-28.5
D:\DevTools\RabbitMQ\rabbitmq-server-4.3.6
D:\DevTools\RabbitMQ\data
D:\DevTools\RabbitMQ\logs
```

项目脚本：

```powershell
.\scripts\rabbitmq\Start-RabbitMQ.ps1
.\scripts\rabbitmq\Status-RabbitMQ.ps1
.\scripts\rabbitmq\Stop-RabbitMQ.ps1
```

Management UI：

```text
http://localhost:15672
```

本地默认账号：

```text
guest
guest
```

guest 账号只能用于本机学习，不能暴露到公网。

## 7. 项目环境变量

后端预计使用：

```text
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=token_mall
MYSQL_USERNAME=token_mall_dev
MYSQL_PASSWORD=<local-password>

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

JWT_SECRET=<at-least-32-bytes>
JWT_EXPIRES_SECONDS=7200
```

不要把真实密码和 JWT 密钥提交到 Git。

## 8. 编码约定

Windows 默认区域为 `zh_CN`，Maven 已显示平台编码为 GBK。项目必须显式使用 UTF-8：

```properties
project.build.sourceEncoding=UTF-8
project.reporting.outputEncoding=UTF-8
```

运行 Spring Boot 时建议加入：

```powershell
$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
```

## 9. 每日启动顺序

1. 启动 MySQL。
2. 启动 Redis。
3. 启动 RabbitMQ。
4. 启动 Spring Boot：

```powershell
.\scripts\dev\Start-Backend.ps1
```

5. 启动 React 开发服务器：

```powershell
.\scripts\dev\Start-Frontend.ps1
```

6. 打开 RabbitMQ Management UI。
7. 打开 Obsidian 知识库。

前端地址：

```text
http://127.0.0.1:5173
```

后端地址：

```text
http://localhost:8080
```

停止服务：

```powershell
.\scripts\dev\Stop-Backend.ps1
.\scripts\dev\Stop-Frontend.ps1
```

## 10. 常见问题

### 10.1 MySQL 服务无法启动

检查：

```powershell
Get-Service MySQL
Get-EventLog -LogName Application -Source MySQL -Newest 20
```

### 10.2 Redis 无法连接

检查：

```powershell
Test-NetConnection localhost -Port 6379
```

### 10.3 RabbitMQ 无法启动

检查：

```powershell
.\scripts\rabbitmq\Status-RabbitMQ.ps1
Get-Content D:\DevTools\RabbitMQ\logs\*.log -Tail 100
```

重点确认：

- `ERLANG_HOME` 是否指向 `D:\DevTools\RabbitMQ\erlang-28.5`。
- Erlang 能否执行 `erl -version`。
- RabbitMQ 数据目录是否可写。
- 5672 和 15672 端口是否被占用。

### 10.4 中文乱码

确认：

- Maven 编码为 UTF-8。
- Java 启动参数包含 `-Dfile.encoding=UTF-8`。
- SQL 客户端使用 `utf8mb4`。
- 编辑器文件编码为 UTF-8。
