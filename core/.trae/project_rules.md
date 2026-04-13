# OWL Core Trae 项目规则

## 项目概述

OWL Agent 核心框架，使用 Spring Boot 容器管理，直接使用 Spring AI。

## 模块架构

```
core/
├── src/main/java/com/owl/core/
│   ├── OwlCoreApplication.java # Spring Boot 启动类
│   ├── common/              # 公共组件
│   │   ├── TraceUtils.java
│   │   └── exception/
│   │       └── OwlException.java
│   ├── config/              # 配置模块
│   │   └── RepositoryConfig.java # 仓库配置
│   ├── task/                # 任务领域
│   ├── memory/              # 记忆领域
│   ├── session/             # 会话领域
│   ├── skills/              # 技能领域
│   │   ├── tools/          # 原子工具（本地能力）
│   │   │   ├── Tool.java           # 工具接口
│   │   │   ├── DirectoryTool.java  # 目录操作工具
│   │   │   ├── FileTool.java       # 文件操作工具
│   │   │   ├── CommandTool.java    # 命令执行工具
│   │   │   └── WebSearchTool.java  # 联网搜索工具
│   │   └── definition/     # 技能定义（提示词 + 工具组合）
│   │       ├── SkillDefinition.java          # 技能定义实体（可持久化，ID为String类型）
│   │       └── SkillDefinitionRepository.java # 技能定义仓库接口（Spring Data JPA）
│   ├── llm/                 # LLM 领域
│   └── persistence/         # 持久化领域
```

## 架构边界定义

### 1. Tool（原子工具）
- **是什么**：最小可用功能单元，纯函数式、无状态、无业务逻辑
- **职责**：只做单一动作：计算、查询、发送等
- **存放规则**：本地 JVM 内通用能力，可被多个 Skill 复用
- **禁止**：禁止业务流程、禁止编排、禁止调用其他 Tool

### 2. Skill（业务技能）
- **是什么**：抽象的业务能力概念，由提示词和工具集合构成
- **职责**：定义如何完成一个完整的业务任务
- **实现方式**：通过提示词编排多个工具完成复杂任务
- **存放规则**：提示词模板 + 工具列表

### 3. MCP（Model Context Protocol）
- **是什么**：跨进程、跨服务、跨语言的工具调用标准协议
- **职责**：统一暴露远程工具接口、协议转换、认证、限流
- **存放规则**：非本 JVM 的能力、公共服务化能力
- **禁止**：禁止写业务逻辑、禁止绑定具体业务场景

### 4. 调用链
用户请求 → Chat 层 → Skill（提示词 + 工具列表）→ Tool（本地能力）/ MCP（远程能力）

## 持久化设计

### SkillDefinition 实体字段
- **id**: 主键（String类型，UUID自动生成）
- **name**: 技能名称（唯一）
- **description**: 技能描述
- **promptTemplate**: 提示词模板
- **toolNames**: 工具名称列表
- **createdAt/updatedAt**: 创建和更新时间

### Repository 使用方式
Repository 通过 Spring Boot 容器自动管理：

```java
@Autowired
private SkillDefinitionRepository repository;

// 使用 Repository
SkillDefinition skill = repository.findById("some-uuid-string").orElse(null);
```

## 基础工具集

### 1. DirectoryTool（目录操作工具）
- **功能**：列出目录内容（文件/子目录）
- **名称**：`listDirectory`
- **参数**：`path` - 目录路径
- **返回**：目录内容列表

### 2. FileTool（文件操作工具）
- **功能**：读取文件内容
- **名称**：`readFile`
- **参数**：`path` - 文件路径
- **返回**：文件内容字符串

- **功能**：写入/覆盖文件
- **名称**：`writeFile`
- **参数**：`path` - 文件路径, `content` - 文件内容
- **返回**：操作结果

### 3. CommandTool（命令执行工具）
- **功能**：执行终端命令（运行、编译、安装）
- **名称**：`runCommand`
- **参数**：`command` - 命令字符串
- **返回**：命令执行结果

### 4. WebSearchTool（联网搜索工具）
- **功能**：联网搜索（获取资料、文档、最新信息）
- **名称**：`webSearch`
- **参数**：`query` - 搜索关键词
- **返回**：搜索结果

## 技能定义示例

### 代码审查技能
- **提示词**：分析代码质量、查找潜在问题、提出改进建议
- **工具组合**：DirectoryTool（获取项目结构）+ FileTool（读取源码）+ CommandTool（运行静态分析工具）

### 文档生成技能
- **提示词**：根据代码和注释生成技术文档
- **工具组合**：FileTool（读取源码）+ WebSearchTool（获取相关资料）+ FileTool（写入文档）

## 依赖关系

```
llm → spring-ai
session → llm
task → session
persistence → task, memory, session
config → persistence
```

## 测试规范

- 测试类放在 `src/test/java`
- 每个测试方法必须有 `@DisplayName` 中文描述
- 测试类命名：`{被测试类}Test`

## LLM 配置

使用 `ChatModelFactory.Builder` 创建 ChatModel：

```java
var chatModel = new ChatModelFactory.Builder()
    .apiKey("your-api-key")
    .model("model-name")
    .baseUrl("https://api.xxx.com/v1")
    .build();
```
