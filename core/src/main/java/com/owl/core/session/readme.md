# Session 会话领域

会话领域负责管理 Agent 的对话会话，是短期记忆的具体实现。

## 模块结构

```
session/
├── model/              # 领域模型
│   ├── Session.java          # 会话模型
│   ├── Message.java          # 消息模型
│   └── SessionStatus.java   # 会话状态枚举
├── repository/         # 仓储接口
│   └── SessionRepository.java
└── service/            # 领域服务接口
    └── SessionManager.java
```

## 关键设计

### 1. 会话模型

```java
public class Session {
    private String id;              // 会话ID
    private String userId;         // 用户ID
    private String title;          // 会话标题
    private SessionStatus status; // 会话状态
    private List<Message> messages; // 消息历史
    private Map<String, Object> context; // 上下文数据
    private Instant createdAt;    // 创建时间
    private Instant updatedAt;    // 最后活动时间
}
```

### 2. 消息模型

```java
public class Message {
    private String id;              // 消息ID
    private MessageRole role;     // 角色：USER, ASSISTANT, SYSTEM
    private String content;        // 消息内容
    private List<ToolCall> toolCalls; // 工具调用记录
    private Map<String, Object> metadata; // 元数据
    private Instant timestamp;     // 时间戳
}
```

### 3. 会话状态

```
ACTIVE    # 活跃中
PAUSED    # 暂停（等待用户输入）
ARCHIVED  # 已归档
CLOSED    # 已关闭
```

### 4. 会话管理器

```java
public interface SessionManager {
    Session create(String userId, String title);
    Session get(String sessionId);
    Message addMessage(String sessionId, Message message);
    List<Message> getHistory(String sessionId);
    void archive(String sessionId);
}
```

## 与 Memory 领域的关系

- Session 是短期记忆的具体实现
- Memory 领域是抽象接口
- Session 实现 MemoryRepository 接口

```
Memory (抽象)
   ↓
Session (具体实现)
```

## 使用场景

### 1. 创建会话

```java
var session = sessionManager.create(userId, "新会话");
```

### 2. 添加消息

```java
var message = new Message(MessageRole.USER, "帮我写个程序");
sessionManager.addMessage(sessionId, message);
```

### 3. 获取历史

```java
var history = sessionManager.getHistory(sessionId);
history.forEach(m -> System.out.println(m.getRole() + ": " + m.getContent()));
```

### 4. 归档会话

```java
sessionManager.archive(sessionId);
```

## 实现顺序

1. SessionStatus 枚举
2. Message 模型
3. Session 模型
4. SessionRepository 接口
5. SessionManager 接口
