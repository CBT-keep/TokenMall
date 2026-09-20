---
type: guide
status: active
tags:
  - tokenmall
  - obsidian
---

# Obsidian 图谱说明

## 图谱如何生成

Obsidian 的 Graph View 会根据以下内容自动生成节点和边：

- WIKILINK 双向链接语法
- 标签
- frontmatter 中的概念引用
- 指向课程、实验和问题的链接

## 本项目的连接规则

| 类型 | 连接目标 |
| --- | --- |
| 课程 | 概念、实验、问题、前后课程 |
| 概念 | 课程、源码路径、相关概念 |
| 实验 | 问题编号、课程、验证结果 |
| 开发日志 | Git 提交、课程日 |
| ADR | 架构决策和影响模块 |

## 查看方式

1. 打开 `E:\new_project\knowledge-vault`。
2. 点击左侧 Graph View。
3. 搜索 `tag:#tokenmall` 查看整个项目。
4. 搜索 `path:"02-Course"` 查看课程结构。
5. 打开某个概念节点，查看反链定位相关课程。

## 自动维护

运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\knowledge\Update-KnowledgeIndex.ps1
```

该脚本会更新：

- [[Course Index]]
- [[Development Log]]
- [[Open Questions]]
