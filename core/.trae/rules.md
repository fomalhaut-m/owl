# OWL Core 项目规则

## 一、代码架构

### 1. 模块划分

```
owl-java/core/
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
│   │   ├── ChatClient.java
│   │   └── ChatModelFactory.java
│   └── persistence/         # 持久化领域
```

### 2. 领域职责

| 领域 | 职责 |
|------|------|
| common | 公共组件：工具类、异常 |
| config | 配置模块：仓库配置等 |
| task | 任务管理：AgentTask、TaskStep |
| memory | 长期记忆：UserMemory |
| session | 短期记忆：Session、Message |
| skills | 技能系统：原子工具、技能定义（提示词+工具组合） |
| llm | LLM 调用：ChatClient、ChatModelFactory |
| persistence | 数据持久化：Repository 接口 |

### 3. 依赖关系

```
llm → spring-ai
session → llm
task → session
persistence → task, memory, session
config → persistence
```

## 二、架构边界定义

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

### 5. 架构红线
1. **Skill 绝不实现能力，只编排能力**
2. **Tool 绝不写业务，只干实事**
3. **MCP 绝不做业务，只做协议暴露**
4. **Controller 绝不直接调 Tool/MCP，必须走 Skill**
5. **所有依赖单向向下，无反向、无循环**

## 三、持久化设计

### 1. 技能定义持久化
- **技术栈**：Spring Data JPA + H2 Database
- **实体**：SkillDefinition（技能定义实体，ID为String类型）
- **仓库接口**：SkillDefinitionRepository（技能定义仓库接口）
- **特点**：文件型数据库，轻量级，适合本地开发
- **实例化方式**：通过 Spring Boot 容器自动管理

### 2. SkillDefinition 实体字段
- **id**: 主键（String类型，UUID自动生成）
- **name**: 技能名称（唯一）
- **description**: 技能描述
- **promptTemplate**: 提示词模板
- **toolNames**: 工具名称列表
- **createdAt/updatedAt**: 创建和更新时间

### 3. Repository 使用方式
Repository 通过 Spring Boot 容器自动管理：

```java
@Autowired
private SkillDefinitionRepository repository;

// 使用 Repository
SkillDefinition skill = repository.findById("some-uuid-string").orElse(null);
```

## 四、基础工具集

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

## 五、技能定义示例

### 代码审查技能
- **提示词**：分析代码质量、查找潜在问题、提出改进建议
- **工具组合**：DirectoryTool（获取项目结构）+ FileTool（读取源码）+ CommandTool（运行静态分析工具）

### 文档生成技能
- **提示词**：根据代码和注释生成技术文档
- **工具组合**：FileTool（读取源码）+ WebSearchTool（获取相关资料）+ FileTool（写入文档）

## 六、单元测试标准

### 1. 测试文件位置

- 单元测试放在 `src/test/java` 目录下
- 包路径与主代码一致
- 测试类命名：`{被测试类}Test`

### 2. 测试命名规范

```java
@DisplayName("测试类中文描述")
class XxxTest {

    @Test
    @DisplayName("测试方法中文描述")
    void testXxx() {
        // 测试代码
    }
}
```

### 3. 测试方法要求

- 每个测试方法必须有 `@DisplayName` 注解
- 使用中文描述测试目的
- 测试方法以 `test` 开头
- 保持测试方法独立，不依赖执行顺序

### 4. 断言规范

```java
// 使用 JUnit 5 断言
import static org.junit.jupiter.api.Assertions.*;

// 常见断言
assertEquals(expected, actual);
assertNotNull(object);
assertNull(object);
assertTrue(condition);
assertFalse(condition);
assertSame(obj1, obj2);
assertThrows(Exception.class, () -> { });
```

### 5. 测试覆盖原则

- 核心类必须有单元测试
- 公共方法需要覆盖
- 边界条件需要测试
- 异常情况需要测试

### 6. Mock 使用原则

- 优先使用真实对象而非 Mock
- 仅对外部依赖（数据库、网络）使用 Mock
- 可以使用内部类实现简单 Mock

```java
// 示例：使用内部类实现 Mock
static class TestChatModel implements ChatModel {
    @Override
    public String call(String message) {
        return "Mock response: " + message;
    }
}
```

## 七、代码规范

### 1. 包命名

- 使用小写字母
- 按功能模块划分包
- 示例：`com.owl.core.llm`、`com.owl.core.common.exception`

### 2. 类命名

- 使用大驼峰命名
- 枚举类以 `Enum` 结尾或使用全大写
- 接口以 `I` 开头或使用名词

### 3. 方法命名

- 使用小驼峰命名
- 动词前缀：`getXxx`、`setXxx`、`createXxx`、`buildXxx`
- 布尔返回值：`isXxx`、`hasXxx`

### 4. 常量命名

- 使用全大写
- 以下划线分隔
- 示例：`MAX_RETRY_COUNT`、`DEFAULT_TIMEOUT`

### 5. 注释规范

- 不添加无意义的注释
- 复杂逻辑添加中文说明
- 公共 API 添加 Javadoc

## 八、构建规范

### 1. 依赖管理

- 使用 Maven 构建
- 依赖版本在 `pom.xml` 中统一管理
- 禁止硬编码版本号

### 2. 构建命令

```bash
# 编译
mvn compile

# 测试
mvn test

# 打包
mvn package
```
