# Task 任务领域

任务领域负责管理 Agent 的任务执行生命周期。

## 模块结构

```
task/
├── model/              # 领域模型
│   ├── TaskStatus.java      # 任务状态枚举
│   ├── StepStatus.java     # 步骤状态枚举
│   ├── AgentTask.java       # 任务模型
│   └── TaskStep.java       # 步骤模型
├── repository/         # 仓储接口
│   ├── AgentTaskRepository.java
│   └── TaskStepRepository.java
└── service/            # 领域服务接口
    ├── TaskPlanner.java    # 任务规划接口
    └── StepExecutor.java   # 步骤执行接口
```

## 关键设计

### 1. 任务状态机

```
PENDING → PLANNING → RUNNING → COMPLETED
                ↓                   ↓
            FAILED ←────────── CANCELLED
```

- **PENDING**: 任务创建，待规划
- **PLANNING**: 规划中（调用 LLM 分解步骤）
- **RUNNING**: 执行中
- **COMPLETED**: 完成
- **FAILED**: 失败
- **CANCELLED**: 取消

### 2. 步骤模型

每个任务包含多个步骤，步骤之间支持：
- 顺序执行
- 并行执行（future）
- 条件分支
- 重试机制

### 3. 任务规划

```java
public interface TaskPlanner {
    AgentTask plan(String userRequest);
}
```

- 接收用户请求
- 调用 LLM 分解为步骤列表
- 返回包含步骤的任务

### 4. 步骤执行

```java
public interface StepExecutor {
    TaskStepResult execute(TaskStep step, SkillRegistry registry);
}
```

- 执行单个步骤
- 调用 SkillRegistry 获取技能
- 返回执行结果

## 核心类说明

### AgentTask

```java
public class AgentTask {
    private String id;                  // 任务ID
    private String userId;              // 用户ID
    private String description;         // 任务描述
    private TaskStatus status;          // 任务状态
    private List<TaskStep> steps;       // 执行步骤
    private Map<String, Object> context;// 上下文数据
    private Instant createdAt;          // 创建时间
    private Instant updatedAt;          // 更新时间
}
```

### TaskStep

```java
public class TaskStep {
    private String id;                 // 步骤ID
    private String taskId;             // 所属任务ID
    private int order;                  // 步骤序号
    private String description;         // 步骤描述
    private StepStatus status;         // 步骤状态
    private String skillName;          // 调用的技能名
    private Map<String, Object> params; // 技能参数
    private Object result;             // 执行结果
    private String error;              // 错误信息
}
```

## 实现顺序

1. TaskStatus / StepStatus 枚举
2. AgentTask / TaskStep 模型
3. AgentTaskRepository / TaskStepRepository 接口
4. TaskPlanner 接口
5. StepExecutor 接口
