# Memory 记忆领域

记忆领域负责管理 Agent 的记忆系统，支持短期记忆和长期记忆。

## 模块结构

```
memory/
├── model/              # 领域模型
│   ├── MemoryType.java      # 记忆类型枚举
│   └── UserMemory.java      # 记忆模型
├── repository/         # 仓储接口
│   └── UserMemoryRepository.java
└── service/            # 领域服务接口
    └── UserMemoryService.java
```

## 关键设计

### 1. 记忆类型

```
SHORT_TERM    # 短期记忆（会话级别，会话结束清除）
LONG_TERM     # 长期记忆（持久化，长期保留）
WORKING       # 工作记忆（当前任务上下文）
```

### 2. 记忆模型

```java
public class UserMemory {
    private String id;           // 记忆ID
    private String userId;       // 用户ID
    private MemoryType type;    // 记忆类型
    private String key;          // 记忆键
    private String content;      // 记忆内容
    private Map<String, Object> metadata; // 元数据
    private Instant createdAt;   // 创建时间
    private Instant updatedAt;   // 更新时间
}
```

### 3. 记忆服务

```java
public interface UserMemoryService {
    void save(UserMemory memory);
    Optional<UserMemory> findById(String id);
    List<UserMemory> findByUserId(String userId, MemoryType type);
    void delete(String id);
}
```

### 4. 记忆检索

- 按用户 ID 检索
- 按类型检索
- 按关键字检索
- 时间范围检索

## 使用场景

### 短期记忆
- 当前会话的对话历史
- 临时计算结果
- 会话结束自动清除

### 长期记忆
- 用户偏好设置
- 历史任务记录
- 重要信息持久化

### 工作记忆
- 当前任务的中间状态
- 步骤间的数据传递

## 实现顺序

1. MemoryType 枚举
2. UserMemory 模型
3. UserMemoryRepository 接口
4. UserMemoryService 接口
