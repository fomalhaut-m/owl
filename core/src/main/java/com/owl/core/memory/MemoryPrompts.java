package com.owl.core.memory;

public final class MemoryPrompts {

    private MemoryPrompts() {
    }

    /**
     * 记忆系统配置提示词
     * <p>
     * 包含记忆系统的完整配置说明，包括目录结构、文件命名规则、
     * 用户配置格式、记忆分级规则和使用示例。
     * </p>
     */
    public static final String MEMORY_SYSTEM_CONFIG = """
            # 记忆系统配置
            
            ## 目录结构
            
            **路径表示法：** `memory/users/{userId}/mnemosyne/config.json`
            
            ```
            memory/
            └── users/
                └── {userId}/
                    └── mnemosyne/          # 记忆系统根目录
                        │   ├── index.md    # 记忆索引
                        │   └── todo.md     # 代办任务
                        ├── working/        # 工作记忆（注入 LLM 上下文）
                        │   ├── P0/         # 最高优先级：当前任务、即时决策
                        │   ├── P1/         # 高优先级：今日重点、关键进展
                        │   ├── P2/         # 中优先级：本周计划、阶段性总结
                        │   └── P3/         # 低优先级：背景信息、参考资料
                        ├── long_term/      # 长期记忆（周期性清理）
                        │   ├── S/          # 核心知识：行业洞察、底层逻辑（30天删除）
                        │   ├── A/          # 重要知识：项目经验、技术方案（14天删除）
                        │   └── B/          # 一般知识：日常记录、临时想法（7天删除）
                        ├── cold_backup/    # 冷归档：已删除记忆的备份（90天清理）
                        ├── logs/           # 操作日志：记录记忆的增删改查历史
                        ├── chat/           # 聊天记录：用户与 LLM 的互动历史
                        └── config.json     # 用户配置：个性化参数设置
                    └── archive/            # 归档区（持久化存储）
                        ├── user_base.md    # 基础信息：姓名、职业、偏好等静态数据
                        └── user_profile.md # 用户画像：行为习惯、兴趣演变等动态档案
            ```

            ## 聊天记忆目录
            
            **路径表示法：** `memory/users/{userId}/mnemosyne/chat/{sessionId}`
            
            示例
            
            ```
            memory/users/12345/mnemosyne/chat/
            └── 20260423_145600.md
            └── 20260423_150000.md
            └── 20260423_150500.md
            ```
            
            ### 聊天记录 chat/
            
            **路径**：`memory/users/{userId}/mnemosyne/chat/{timestamp}.md`
            
            **作用**：保存用户与 LLM 的互动历史，供梦工厂整理使用。
            
            **格式**：
            ```markdown
            # 对话 {sessionId}
            
            ## 元数据
            - 开始时间：{YYYYMMDD_HHmmss}
            - 结束时间：{YYYYMMDD_HHmmss}
            - 消息数：{count}
            - 用户：{userId}
            
            ## 对话
            ### 用户
            {用户消息内容}
            
            ### 助手
            {助手回复内容}
            
            ### 用户
            {用户消息内容}
            ...
            ```
            
            ### 归档区 archive/
            
            **路径**：`memory/users/{userId}/archive/`
            
            **作用**：持久化存储的用户基础信息和画像，不会被自动清理。
            
            #### user_base.md
            
            **路径**：`memory/users/{userId}/archive/user_base.md`
            
            **作用**：用户的静态基础信息，一次性设置，长期有效。
            
            **格式**：
            ```markdown
            # 用户基础信息
            
            ## 基本资料
            - 姓名：{name}
            - 昵称：{nickname}
            - 职业：{occupation}
            
            ## 联系方式
            - 邮箱：{email}
            - 其他：{contact}
            
            ## 固定偏好
            - 语言：{language}
            - 时区：{timezone}
            - 交互风格：{style}
            
            ## 其他
            - 创建时间：{YYYYMMDD_HHmmss}
            ```
            
            #### user_profile.md
            
            **路径**：`memory/users/{userId}/archive/user_profile.md`
            
            **作用**：用户画像，动态更新，记录行为习惯和兴趣演变。
            
            **格式**：
            ```markdown
            # 用户画像
            
            ## 行为习惯
            - 活跃时段：{morning/afternoon/evening}
            - 交互频率：{high/medium/low}
            - 偏好方式：{directive/exploratory}
            
            ## 兴趣领域
            - 技术栈：{tech1}, {tech2}
            - 行业：{industry}
            - 关注点：{focus}
            
            ## 历史偏好演变
            ### {时间}
            - {偏好变化描述}
            
            ## 重要标记
            - 禁忌：{taboo1}, {taboo2}
            - 敏感词：{sensitive}
            ```

            
            ## 文件命名
            
            格式：`{name}.md`
            规则：
            1. **字符集**：仅限小写字母 `a-z`、数字 `0-9` 和下划线 `_`。
            2. **分隔符**：使用下划线 `_` 替代空格，禁止使用中划线 `-` 或特殊符号。
            3. **语义化**：文件名应清晰描述记忆核心内容（如 `java_stream_api.md`）。
            4. **唯一性**：在同一目录下文件名必须唯一，避免覆盖。
            
            示例：`java_generics.md`、`meeting_notes_20260423.md`
            
            ## 目录内容定义
            
            ### 工作记忆 working/
            
            **路径**：`memory/users/{userId}/mnemosyne/working/{priority}/{name}.md`
            
            **作用**：当前活跃的记忆，直接带入 LLM 上下文的优先级最高内容。
            
            **优先级**：
            - `P0/`: 当前任务、即时决策，需立即响应
            - `P1/`: 今日重点、关键进展，本日内频繁访问
            - `P2/`: 本周计划、阶段性总结，本周内参考
            - `P3/`: 背景信息、参考资料，补充 contexto
            
            **格式**：
            ```markdown
            # {记忆标题}
            
            ## 元数据
            - 创建时间：{YYYYMMDD_HHmmss}
            - 更新时间：{YYYYMMDD_HHmmss}
            - 优先级：{P0/P1/P2/P3}
            - 标签：{tag1}, {tag2}
            
            ## 内容
            {正文内容}
            
            ## 关联
            - 相关记忆：{path1}, {path2}
            ```
            
            ### 长期记忆 long_term/
            
            **路径**：`memory/users/{userId}/mnemosyne/long_term/{level}/{name}.md`
            
            **作用**：周期性清理的记忆，按权重分级存储。
            
            **分级**：
            - `S/`: 核心知识，30天删除，行业洞察、底层逻辑
            - `A/`: 重要知识，14天删除，项目经验、技术方案
            - `B/`: 一般知识，7天删除，日常记录、临时想法
            
            **格式**：
            ```markdown
            # {记忆标题}
            
            ## 元数据
            - 创建时间：{YYYYMMDD_HHmmss}
            - 级别：{S/A/B}
            - 上次激活：{YYYYMMDD_HHmmss}
            - 激活次数：{count}
            
            ## 内容
            {正文内容}
            
            ## 摘要
            {核心要点摘要（50字以内）}
            ```
            
            ### 冷归档 cold_backup/
            
            **路径**：`memory/users/{userId}/mnemosyne/cold_backup/{timestamp}_{name}.md`
            
            **作用**：已删除记忆的备份，90天后自动清理，可恢复。
            
            **格式**：
            ```markdown
            # {原记忆标题}
            
            ## 元数据
            - 原路径：{original_path}
            - 删除时间：{YYYYMMDD_HHmmss}
            - 过期时间：{YYYYMMDD_HHmmss}
            - 删除原因：{主动删除/过期清理/覆盖}
            
            ## 原内容
            {记忆原内容}
            ```
            
            ### 操作日志 logs/
            
            **路径**：`memory/users/{userId}/mnemosyne/logs/{date}.md`
            
            **作用**：记录记忆的增删改查操作历史，便于审计和追溯。
            
            **格式**：
            ```markdown
            # 操作日志 {YYYYMMDD}
            
            ## 时间线
            - {HHmmss} [操作] {路径} - {摘要}
            
            ## 统计
            - 新增：{count}
            - 读取：{count}
            - 更新：{count}
            - 删除：{count}
            ```
            
            **操作类型**：SAVE、GET、UPDATE、DELETE、QUERY
            
            ## 记忆文件格式约束
            
            1. **文件类型**：必须为 `.md` (Markdown) 格式。
            2. **编码要求**：统一使用 `UTF-8` 编码，支持多语言（尤其是中文）。
            3. **内容结构**：建议包含标题、创建时间、最后更新时间及正文。
            4. **路径规范**：所有路径分隔符在逻辑上统一为正斜杠 `/`，确保跨平台兼容。
            5. **大小限制**：单文件建议不超过 10KB，过大的记忆应进行拆分或压缩。
            
            ## 索引文件 index.md
            
            **路径**：`memory/users/{userId}/mnemosyne/index.md`
            
            **作用**：维护记忆索引，提供快速检索能力，避免重复记忆和语义冗余。
            
            **格式**：
            ```markdown
            # 记忆索引
            
            ## 最近更新
            - {时间} {路径} {摘要}
            
            ## 记忆点
            ### 主题分类
            - {path}: {summary}
            - {path}: {summary}
            
            ## 引用关系
            - {source_path} → {target_path}
            ```
            
            **字段说明**：
            - `时间`：格式 `YYYYMMDD_HHmmss`
            - `路径`：记忆文件的相对路径
            - `摘要`：记忆内容的简要描述（50字以内）
            
            ## 待办文件 todo.md
            
            **路径**：`memory/users/{userId}/mnemosyne/todo.md`
            
            **作用**：记录用户的待办任务和待回复内容，作为主动触发的提醒。
            
            **格式**：
            ```markdown
            # 待办任务
            
            ## 待回复
            - [{优先级}] {内容} @{时间}
            
            ## 待处理
            - [{优先级}] {内容} @{时间}
            
            ## 已完成
            - [{时间}] {内容}
            ```
            
            **字段说明**：
            - `优先级`：P0（紧急）、P1（重要）、P2（普通）、P3（参考）
            - `时间`：格式 `YYYYMMDD_HHmmss` 或相对时间
            - `@时间`：到期时间或提醒时间
            
            ## 用户记忆配置 config.json
            
            ```json
            {
              "contextMaxTokenRate": 0.3,        // 上下文 Token 使用率（0.0-1.0）
              "coldBackupExpireDays": 90,        // 冷备份保留天数
              "sDowngradeDays": 14,              // S→A 降级天数
              "sDeleteDays": 30,                 // S 级删除天数
              "aDowngradeDays": 7,               // A→B 降级天数
              "aDeleteDays": 14,                 // A 级删除天数
              "bDeleteDays": 7,                  // B 级删除天数
              "semanticSimilarThreshold": 0.85,  // 语义相似度阈值（0.0-1.0）
              "compressRatio": 10                // 压缩比率
            }
            ```
            
            ## 记忆分级规则
            
            - **working/**：活跃记忆，频繁读写
            - **long_term/S/**：核心知识，保留 30 天
            - **long_term/A/**：重要知识，保留 14 天
            - **long_term/B/**：一般知识，保留 7 天
            - **cold_backup/**：已删除备份，90 天后清理
            
            ## 使用示例
            
            **保存记忆：**
            ```
            路径：working/P0/recent_thought.md
            内容：今天学到了 Java 泛型知识
            ```
            
            **检索记忆：**
            - `getMemory(userId, path)` - 按路径获取
            - `getMemoryPaths(userId)` - 获取所有路径
            - `getMemoryPathsByTimeRange(userId, startTime, endTime)` - 按时间范围获取
            
            **配置管理：**
            - `getUserConfig(userId)` - 获取用户配置
            - `saveUserConfig(userId, config)` - 保存用户配置
            """;

}