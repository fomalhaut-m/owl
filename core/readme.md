# OWL Core

OWL Agent 核心框架，直接使用 Spring AI，无 Spring 容器依赖。

## 模块架构

```
core/
├── pom.xml
└── src/main/java/com/owl/core/
    ├── task/                  # 任务领域
    │   ├── model/             # 领域模型
    │   │   ├── AgentTask.java
    │   │   ├── TaskStep.java
    │   │   ├── TaskStatus.java
    │   │   └── StepStatus.java
    │   ├── repository/        # 仓储接口
    │   │   ├── AgentTaskRepository.java
    │   │   └── TaskStepRepository.java
    │   └── service/          # 领域服务接口
    │       ├── TaskPlanner.java
    │       └── StepExecutor.java
    │
    ├── memory/                # 记忆领域
    │   ├── model/             # 领域模型
    │   │   ├── UserMemory.java
    │   │   └── MemoryType.java
    │   ├── repository/        # 仓储接口
    │   │   └── UserMemoryRepository.java
    │   └── service/          # 领域服务接口
    │       └── UserMemoryService.java
    │
    ├── skills/                # 技能领域
    │   ├── SkillRegistry.java # 技能注册中心
    │   ├── AgentSkill.java    # 技能接口
    │   ├── impl/              # 技能实现
    │   │   ├── FileSkill.java
    │   │   ├── BashSkill.java
    │   │   └── SearchSkill.java
    │   └── annotation/        # 技能注解
    │       └── Skill.java
    │
    ├── llm/                   # LLM 领域
    │   ├── ChatClient.java    # 聊天客户端
    │   ├── prompt/            # Prompt 模板
    │   │   ├── PromptTemplate.java
    │   │   └── OutputParser.java
    │   └── tools/             # 工具定义
    │       └── ToolCallback.java
    │
    ├── persistence/           # 持久化领域
    │   ├── Repository.java    # 仓储接口
    │   ├── file/              # 文件存储实现
    │   └── jpa/               # JPA 存储实现
    │
    └── common/                # 公共组件
        ├── TraceUtils.java
        └── exception/
            └── OwlException.java
```

## 领域划分

| 领域 | 说明 | 核心类 |
|------|------|--------|
| **task** | 任务管理 | AgentTask, TaskStep, TaskPlanner |
| **memory** | 记忆管理 | UserMemory, UserMemoryService |
| **skills** | 技能系统 | AgentSkill, SkillRegistry |
| **llm** | LLM 调用 | ChatClient, PromptTemplate |
| **persistence** | 持久化 | Repository 接口 |
| **common** | 公共组件 | 工具类、异常 |

## 依赖关系

```
llm → spring-ai-core
skills → llm
task → memory
persistence → task, memory
```

## 实现顺序

### 第一阶段：基础领域（无外部依赖）

**1. common - 公共组件**
- `OwlException.java` - 基础异常类
- `TraceUtils.java` - 追踪工具

**2. task - 任务领域**
- `TaskStatus.java` - 任务状态枚举
- `StepStatus.java` - 步骤状态枚举
- `AgentTask.java` - 任务模型
- `TaskStep.java` - 步骤模型
- `AgentTaskRepository.java` - 任务仓储接口
- `TaskStepRepository.java` - 步骤仓储接口
- `TaskPlanner.java` - 任务规划接口
- `StepExecutor.java` - 步骤执行接口

**3. memory - 记忆领域**
- `MemoryType.java` - 记忆类型枚举
- `UserMemory.java` - 记忆模型
- `UserMemoryRepository.java` - 仓储接口
- `UserMemoryService.java` - 服务接口

### 第二阶段：LLM 领域

**4. llm - LLM 领域**
- `PromptTemplate.java` - Prompt 模板
- `OutputParser.java` - 输出解析器
- `ToolCallback.java` - 工具回调接口
- `ChatClient.java` - 聊天客户端（依赖 spring-ai-core）

### 第三阶段：技能领域

**5. skills - 技能领域**
- `Skill.java` - 技能注解
- `AgentSkill.java` - 技能接口
- `SkillRegistry.java` - 技能注册中心
- `FileSkill.java` - 文件技能实现
- `BashSkill.java` - Shell 技能实现
- `SearchSkill.java` - 搜索技能实现

### 第四阶段：持久化

**6. persistence - 持久化领域**
- `Repository.java` - 基础仓储接口
- `file/FileRepository.java` - 文件存储实现
- `jpa/JpaRepository.java` - JPA 存储实现

## 设计原则

### 1. 领域驱动设计 (DDD)
- 每个领域独立内聚
- 领域间通过接口通信
- 无循环依赖

### 2. 无 Spring 容器
- 直接使用 Spring AI API
- 手动创建实例

### 3. 领域可独立使用
- task 可单独使用
- memory 可单独使用
- skills 可单独使用

## 快速开始

### 1. pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.owl</groupId>
    <artifactId>owl-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M1</spring-ai.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-core</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
    </dependencies>
</project>
```

### 2. 使用示例

```java
public class Main {
    public static void main(String[] args) {
        // 创建 ChatClient
        var chatClient = new ChatClient(
            OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build()
        );

        // 创建技能注册
        var registry = new SkillRegistry();
        registry.register(new FileSkill());
        registry.register(new BashSkill());

        // 创建任务规划
        var planner = new TaskPlanner(chatClient);
        var task = planner.plan("帮我写一个 hello world 程序");

        // 执行任务
        var executor = new StepExecutor(registry);
        executor.execute(task);
    }
}
```

## 构建与测试

```bash
mvn clean package
mvn test
```
