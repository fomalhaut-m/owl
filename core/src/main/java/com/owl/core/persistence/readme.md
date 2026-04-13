# Persistence 持久化领域

持久化领域负责数据的存储，提供统一的仓储接口和多种实现。

## 模块结构

```
persistence/
├── Repository.java    # 基础仓储接口
├── file/              # 文件存储实现
│   └── FileRepository.java
└── jpa/               # JPA 存储实现
    └── JpaRepository.java
```

## 关键设计

### 1. 统一仓储接口

```java
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void delete(ID id);
    long count();
}
```

### 2. 泛型支持

- 支持任意实体类型
- 支持任意主键类型
- 具体实现自行定义

### 3. 文件存储实现

```java
public class FileRepository<T> implements Repository<T, String> {
    private final Path basePath;
    private final Class<T> entityClass;
    private final ObjectMapper mapper;

    @Override
    public T save(T entity) {
        // 序列化为 JSON 文件
    }

    @Override
    public Optional<T> findById(String id) {
        // 从 JSON 文件反序列化
    }
}
```

特点：
- 开发阶段使用
- 纯文件系统，无需数据库
- 数据存储为 JSON 文件

### 4. JPA 存储实现

```java
public class JpaRepository<T, ID> implements Repository<T, ID> {
    private final JpaRepository<T, ID> jpaRepository;

    @Override
    public T save(T entity) {
        return jpaRepository.save(entity);
    }
}
```

特点：
- 生产环境使用
- 支持 MySQL、PostgreSQL 等
- 需要 JPA 依赖

## 存储策略切换

```java
// 开发环境：使用文件存储
Repository<Task, String> taskRepo = new FileRepository<>(path, Task.class);

// 生产环境：使用 JPA
Repository<Task, String> taskRepo = new JpaRepository<>(jpaTemplate);
```

## 实现顺序

1. Repository 基础接口
2. FileRepository 实现
3. JpaRepository 实现
