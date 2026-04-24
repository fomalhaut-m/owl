package com.owl.core.memory;

/**
 * 记忆整理梦工厂提示词
 * <p>
 * 基于聊天记录的记忆离线整理提示词，复刻人脑睡眠时的记忆巩固与整理机制。
 * 自动从聊天记录中提取有价值的信息，生成符合记忆分级规则的记忆内容。
 * </p>
 *
 * <h3>使用工具：</h3>
 * <ul>
 *   <li><b>chat_history_get</b> - 获取指定时间范围的聊天记录</li>
 *   <li><b>getUserConfig</b> - 获取用户记忆配置</li>
 *   <li><b>saveMemory</b> - 保存记忆到指定路径</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>获取用户记忆配置 config.json</li>
 *   <li>根据配置的时间参数调用 chat_history_get 获取聊天记录</li>
 *   <li>分析聊天内容，提取有价值的记忆点</li>
 *   <li>按记忆分级规则分类存储到 Mnemosyne 目录</li>
 * </ol>
 *
 * @author Owl Team
 * @see MemoryPrompts#MEMORY_SYSTEM_CONFIG
 */
public final class DreamPrompts {

    private DreamPrompts() {
    }

    /**
     * 梦工厂系统配置提示词
     * <p>
     * 包含记忆整理的核心指令、过滤规则和输出格式。
     * 继承 MemoryPrompts.MEMORY_SYSTEM_CONFIG 的目录结构规范。
     * </p>
     */
    public static final String DREAM_SYSTEM_CONFIG = """
            # 记忆整理梦工厂 - 系统配置

            ## 核心任务
            你现在需要执行「梦工厂记忆整理」任务，复刻人脑睡眠时的记忆巩固与整理机制。
            从聊天记录中自动提取有价值的信息，生成符合记忆分级规则的记忆内容，
            并保存到 `memory/users/{userId}/Mnemosyne` 目录下的对应位置。

            ## 继承配置
            """ + MemoryPrompts.MEMORY_SYSTEM_CONFIG + """

            ## 输入参数
            - `userId`: 用户唯一标识
            - `config`: 用户记忆配置（从 getUserConfig 获取）
            - `chatHistory`: 聊天记录列表（从 chat_history_get 获取）

            ## 执行流程（必须严格按顺序执行）

            ### 第1步：获取用户配置
            调用 `getUserConfig(userId)` 获取用户的记忆配置，主要使用以下字段：
            - `coldBackupExpireDays`: 冷备份保留天数（默认90）
            - `sDowngradeDays`: S级降级天数（默认14）
            - `sDeleteDays`: S级删除天数（默认30）
            - `aDowngradeDays`: A级降级天数（默认7）
            - `aDeleteDays`: A级删除天数（默认14）
            - `bDeleteDays`: B级删除天数（默认7）
            - `compressRatio`: 压缩比率（默认10）

            ### 第2步：获取聊天记录
            根据用户配置的时间参数，调用 `chat_history_get` 获取需要处理的聊天记录：
            - 时间范围：建议从当前时间往前推 7 天
            - 参数：`startTime`（开始时间戳）、`endTime`（结束时间戳）

            ### 第3步：分析聊天内容
            对每条聊天记录进行分析，提取以下类型的记忆点：
            1. **用户偏好**：用户的偏好设置、禁忌习惯、固定规则
            2. **用户决策**：用户做出的重要决策、选择
            3. **知识沉淀**：用户分享的技术知识、项目经验、行业洞察
            4. **任务进度**：用户的任务进展、待办事项
            5. **重要结论**：用户与模型达成的结论、共识

            ### 第4步：记忆分级
            根据记忆点的重要性和时效性，将其分类到对应的目录：
            - **S级 (long_term/S/)**: 核心知识、行业洞察、底层逻辑，保留30天
            - **A级 (long_term/A/)**: 重要知识、项目经验、技术方案，保留14天
            - **B级 (long_term/B/)**: 一般知识、日常记录、临时想法，保留7天
            - **working/**: 活跃记忆，直接带入上下文

            ### 第5步：保存记忆
            使用 `saveMemory(userId, path, content)` 保存记忆文件：
            - 路径格式：`{category}/{priority}/{memory_name}.md`
            - 文件名：使用小写字母、数字和下划线，语义化命名

            ## 输出规范
            你必须依次输出：
            1. 【用户配置解析结果】
            2. 【聊天记录分析结果】
            3. 【记忆点提取明细】
            4. 【记忆分级结果】
            5. 【保存操作结果】
            6. 【最终校验结果】

            不输出任何额外的解释、说明、寒暄话术。
            """;

    /**
     * 记忆点提取提示词
     * <p>
     * 指导如何从聊天记录中提取有价值的记忆点。
     * </p>
     */
    public static final String MEMORY_EXTRACTION = """
            【记忆点提取规则】

            你需要从每条聊天记录中提取有价值的记忆点，分类规则如下：

            1. **用户偏好类**
               - 用户的固定偏好、禁忌习惯
               - 用户明确要求的规则、约束
               - 用户的交互方式偏好
               → 保存到：working/P1/

            2. **用户决策类**
               - 用户的重大决策、选择
               - 用户确认的结论、共识
               - 用户的核心目标
               → 保存到：working/P0/

            3. **知识沉淀类**
               - 用户分享的技术知识
               - 项目经验总结
               - 行业洞察
               → 保存到：long_term/S/ 或 long_term/A/

            4. **任务进度类**
               - 待办事项
               - 任务进展
               - 阶段性成果
               → 保存到：working/P1/ 或 working/P2/

            5. **一般记录类**
               - 日常对话记录
               - 临时想法
               - 不重要的参考信息
               → 保存到：long_term/B/

            【记忆点格式】
            每条记忆点应包含：
            - 标题：简洁描述记忆内容
            - 来源：对应的聊天记录
            - 摘要：核心信息
            - 元数据：创建时间、重要性等级
            """;

    /**
     * 记忆分级提示词
     * <p>
     * 指导如何根据用户配置对记忆进行分级。
     * </p>
     */
    public static final String MEMORY_CLASSIFICATION = """
            【记忆分级规则】

            根据用户配置中的时间参数，确定每个记忆点的保留等级：

            ## S级 - 核心知识（保留30天）
            - 行业深度洞察、底层逻辑
            - 核心技术方案、架构设计
            - 用户核心目标、长期规划
            标准：`sDeleteDays` 天后删除

            ## A级 - 重要知识（保留14天）
            - 项目经验、技术总结
            - 重要决策、结论
            - 阶段性成果
            标准：`aDeleteDays` 天后删除

            ## B级 - 一般知识（保留7天）
            - 日常记录、临时想法
            - 参考信息、补充资料
            - 不重要的对话记录
            标准：`bDeleteDays` 天后删除

            ## Working - 活跃记忆
            - 当前任务相关
            - 今日重点事项
            - 频繁访问的内容

            【分级决策树】
            1. 该记忆是否与当前任务直接相关？ → working/
            2. 该记忆是否��用户核心目标相关？ → S级
            3. 该知识是否可复用？ → A级
            4. 其他 → B级
            """;

    /**
     * 固定产出结果格式
     * <p>
     * 梦工厂执行完成后的输出格式规范。
     * </p>
     */
    public static final String OUTPUT_FORMAT = """
            【梦工厂输出规范】

            必须按顺序输出以下6个模块：

            1. 【用户配置解析结果】
               - coldBackupExpireDays: {值}
               - sDowngradeDays: {值}
               - sDeleteDays: {值}
               - aDowngradeDays: {值}
               - aDeleteDays: {值}
               - bDeleteDays: {值}
               - compressRatio: {值}

            2. 【聊天记录分析结果】
               - 获取记录数：{数量}
               - 时间范围：{startTime} - {endTime}
               - 记录列表：[记录摘要]

            3. 【记忆点提取明细】
               - 偏好类：{数量}
               - 决策类：{数量}
               - 知识类：{数量}
               - 进度类：{数量}
               - 记录类：{数量}

            4. 【记忆分级结果】
               - S级：{数量}，路径：long_term/S/
               - A级：{数量}，路径：long_term/A/
               - B级：{数量}，路径：long_term/B/
               - Working：{数量}，路径：working/

            5. 【保存操作结果】
               - 成功保存：{数量}
               - 失败：{数量}（如有过）
               - 详细路径列表

            6. 【最终校验结果】
               - 配置合规性：{通过/不通过}
               - 记忆点完整性：{通过/不通过}
               - 分级正确性：{通过/不通过}
               - 保存状态：{成功/失败}
            """;
}