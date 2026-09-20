---
type: course-day
day: 2
status: planned
tags:
  - tokenmall
  - course
  - mysql
  - crud
concepts:
  - "[[MySQL 事务与锁]]"
code_paths:
  - db/01_schema.sql
  - backend/src/main/java/com/tokenmall/common
  - backend/src/main/java/com/tokenmall/catalog
related:
  - "[[Day-01 - 环境、项目与领域认知]]"
  - "[[Day-03 - Spring Security 与 JWT]]"
---

# Day 02：SQL、模块与简单 CRUD

## 今日目标

- 理解商城表结构和字段含义。
- 理解 MyBatis-Plus 的 Entity、Mapper、Service、Controller 分层。
- 完成分类、商品、SKU 和库存的基础 CRUD 阅读与测试。
- 学会通过 SQL 日志追踪一次请求。

## 8 小时安排

| 时间段 | 内容 |
| --- | --- |
| 上午一 | 阅读数据库设计和 SQL 表定义 |
| 上午二 | 追踪管理端分类和商品 CRUD |
| 下午一 | 使用 EXPLAIN 分析列表和详情查询 |
| 下午二 | 补充简单查询和接口验证 |
| 晚上 | 整理数据字典，提交 `day02` |

## 核心概念

- Entity 对应数据库表，DTO 对应接口输入输出。
- Mapper 负责数据库访问。
- Service 负责业务规则和事务边界。
- Controller 只负责协议转换和参数校验。
- 逻辑删除。
- 分页。
- 索引与查询条件匹配。
- 数据库快照和商品快照的区别。

## 代码入口

```text
backend/src/main/java/com/tokenmall/common
backend/src/main/java/com/tokenmall/catalog/controller
backend/src/main/java/com/tokenmall/catalog/service
backend/src/main/java/com/tokenmall/catalog/mapper
backend/src/main/java/com/tokenmall/catalog/entity
backend/src/main/java/com/tokenmall/catalog/dto
```

重点类：

- `ProductController`
- `ProductService`
- `ProductMapper`
- `Product`
- `ProductSku`
- `Inventory`
- `CategoryController`

## 实践任务

- [ ] 为一个完整商品列表请求画出调用链。
- [ ] 找到商品新增、修改、上下架和逻辑删除接口。
- [ ] 找到 SKU 新增和库存调整接口。
- [ ] 打开 MyBatis SQL 日志，记录实际执行 SQL。
- [ ] 使用 `EXPLAIN` 分析商品列表和详情查询。
- [ ] 确认分页参数和统一响应结构。
- [ ] 在前端管理页面新增一个测试商品并删除。

## MyBatis-Plus 学习点

需要区分：

| 场景 | 推荐方式 |
| --- | --- |
| 简单按 ID 查询 | MyBatis-Plus |
| 简单分页 | MyBatis-Plus |
| 条件库存扣减 | 手写 SQL |
| 秒杀库存原子更新 | 手写 SQL |
| 多表聚合查询 | 手写 SQL |
| 消费幂等插入 | 手写 SQL |

## 实验

### 实验 1：观察 SQL

调用：

```http
GET /api/v1/products?type=TOKEN_PACK&page=1&size=10
```

记录：

- Controller 入参。
- Service 条件。
- Mapper SQL。
- 返回给前端的 DTO。

### 实验 2：EXPLAIN

```sql
EXPLAIN SELECT *
FROM product
WHERE product_type = 'TOKEN_PACK'
  AND status = 1
  AND deleted = 0
ORDER BY sort_order;
```

判断是否使用 `idx_type_status_sort`。

### 实验 3：逻辑删除

删除一个测试商品后：

- 检查 `deleted` 字段。
- 确认列表不再返回。
- 思考逻辑删除对唯一索引的影响。

## 验收标准

- [ ] 能解释每个核心表的作用。
- [ ] 能说出一个请求经过哪些层。
- [ ] 能根据日志找到真实 SQL。
- [ ] 能判断列表查询是否使用索引。
- [ ] 管理端基础 CRUD 可运行。

## 常见错误

- Controller 中编写复杂业务。
- 用 Entity 直接作为接口响应。
- 删除使用物理删除，导致历史订单引用失效。
- 查询条件变化后没有新增索引。
- 分页查询返回全部字段。

## 面试问题

- MyBatis-Plus 适合哪些 CRUD，什么时候必须手写 SQL？
- 为什么订单要保存商品快照？
- 逻辑删除和物理删除有什么区别？
- 为什么模糊查询 `%keyword%` 可能导致索引失效？

## 今日复盘

- 实际完成：
- 关键证据：
- 遗留问题：
- 下一个最小行动：

## 关联

- 上一天：[[Day-01 - 环境、项目与领域认知]]
- 下一天：[[Day-03 - Spring Security 与 JWT]]
- 概念：[[MySQL 事务与锁]]
