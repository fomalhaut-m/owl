# OWL Agent 项目规范

## Tool 命名规范

### 命名规则

| 规则 | 说明 |
|------|------|
| 格式 | `{模块}_{资源}_{动作}`，使用 snake_case |
| 前缀 | 必须以 `owl_` 开头 |
| 示例 | `owl_memory_save`, `owl_agent_config_get`, `owl_time_current_system` |

### 已注册的 Tool 名称

| Tool Name | 类 | 方法 |
|-----------|-----|------|
| owl_memory_save | MemoryTools | saveMemory |
| owl_memory_get | MemoryTools | getMemory |
| owl_memory_delete | MemoryTools | deleteMemory |
| owl_memory_list_all | MemoryTools | getMemoryPaths |
| owl_memory_list_by_time | MemoryTools | getMemoryPathsByTimeRange |
| owl_memory_config_save | MemoryTools | saveUserConfig |
| owl_agent_config_set | AgentTools | settingUserConfig |
| owl_agent_config_get | AgentTools | getUserConfig |
| owl_chat_history_get | ChatTools | getChatHistory |
| owl_time_current_system | TimeTools | getCurrentSystemTime |
| owl_time_current_by_zone | TimeTools | getCurrentTimeByZone |

### 验证机制

项目启动时 `ToolValidationRunner` 会自动扫描所有 `ToolComponent` 子类：
- 如果发现重复的 `@Tool(name = "xxx")`，启动失败
- 详情见 `core/src/main/java/com/owl/core/skills/tools/ToolNameValidator.java`

### 新增 Tool 流程

1. 按照命名规则命名（owl_前缀 + 模块_资源_动作）
2. 检查不与已有名称重复
3. 更新 `ToolComponent.java` 注释中的表格
4. 更新本规则文件中的表格
