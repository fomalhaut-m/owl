package com.owl.core.skills;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体技能配置类
 * <p>
 * 封装智能体的各种提示词模板和配置，定义 AI 的行为模式和能力边界。
 * 与 {@link DefaultPrompts} 配合使用，可以快速初始化或自定义智能体的各项配置。
 * </p>
 *
 * <h3>配置项说明：</h3>
 * <ul>
 *   <li><b>agents</b> - 工作流程：AI 的执行步骤和任务处理逻辑</li>
 *   <li><b>soul</b> - 行为准则：核心价值观、行为边界和交互原则</li>
 *   <li><b>identity</b> - 智能体档案：身份、角色定位和专业领域</li>
 *   <li><b>user</b> - 用户档案：用户个性化信息和偏好</li>
 *   <li><b>tools</b> - 工具&本地笔记：可用工具集和本地知识库</li>
 *   <li><b>bootstrap</b> - 初始引导脚本：启动时的初始化指令</li>
 *   <li><b>heartbeat</b> - 心跳检查模板：状态检测模板</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 使用默认配置
 * SkillConfig config = new SkillConfig();
 * config.setAgents(DefaultPrompts.AGENTS_DEFAULT);
 * config.setSoul(DefaultPrompts.SOUL_DEFAULT);
 * config.setIdentity(DefaultPrompts.IDENTITY_DEFAULT);
 * // ... 设置其他字段
 *
 * // 2. 自定义配置
 * SkillConfig customConfig = new SkillConfig();
 * customConfig.setAgents("# 自定义工作流程\n...");
 * customConfig.setSoul("# 自定义行为准则\n...");
 * // ... 其他自定义
 *
 * // 3. 混合使用
 * SkillConfig mixedConfig = new SkillConfig();
 * mixedConfig.setAgents(customAgents);      // 自定义
 * mixedConfig.setSoul(DefaultPrompts.SOUL_DEFAULT); // 默认值
 * // ... 其他混合配置
 * }</pre>
 *
 * <h3>配置建议：</h3>
 * <ul>
 *   <li>✅ 工作流程应清晰明确，包含完整的处理步骤</li>
 *   <li>✅ 行为准则应符合业务规范和法律法规</li>
 *   <li>✅ 智能体档案应准确反映实际能力和定位</li>
 *   <li>✅ 工具列表应与实际可用的工具保持一致</li>
 *   <li>⚠️ 避免过于复杂的提示词，保持简洁明了</li>
 *   <li>⚠️ 定期审查和更新配置，确保有效性</li>
 * </ul>
 *
 * @author OWL Team
 * @version 1.0
 * @see DefaultPrompts
 * @since 2026-04-16
 */
@Data
@NoArgsConstructor
public class SkillConfig {

    /**
     * 工作流程
     * <p>
     * 描述智能体的执行步骤和任务处理逻辑。
     * 指导 AI 如何系统性地理需求和响应用户请求。
     * </p>
     *
     * @see DefaultPrompts#AGENTS_DEFAULT
     */
    private String agents;
    
    /**
     * 行为准则
     * <p>
     * 定义智能体的核心价值观和交互原则。
     * 确保 AI 在各种场景下都能保持专业、安全、可靠的表现。
     * </p>
     *
     * @see DefaultPrompts#SOUL_DEFAULT
     */
    private String soul;
    
    /**
     * 智能体档案
     * <p>
     * 描述智能体的身份、角色定位和专业领域。
     * 帮助用户了解 AI 的能力和适用范围。
     * </p>
     *
     * @see DefaultPrompts#IDENTITY_DEFAULT
     */
    private String identity;
    
    /**
     * 用户档案
     * <p>
     * 存储和管理用户的个性化信息和偏好。
     * 通常由系统根据用户交互历史动态维护。
     * </p>
     *
     * @see DefaultPrompts#USER_DEFAULT
     */
    private String user;
    
    /**
     * 工具&本地笔记
     * <p>
     * 描述可用的工具集和本地知识库。
     * 帮助 AI 了解可以调用的外部能力和项目特定的上下文信息。
     * </p>
     *
     * @see DefaultPrompts#TOOLS_DEFAULT
     */
    private String tools;

    /**
     * 初始引导脚本
     * <p>
     * 智能体启动时的初始化指令。
     * 在每次会话开始时提供给 AI，确保其处于正确的状态。
     * </p>
     *
     * @see DefaultPrompts#BOOTSTRAP_DEFAULT
     */
    private String bootstrap;
    
    /**
     * 心跳检查模板
     * <p>
     * 用于定期检测智能体状态的模板。
     * 包含占位符，在实际使用时会被替换为真实值。
     * </p>
     *
     * @see DefaultPrompts#HEARTBEAT_DEFAULT
     */
    private String heartbeat;

}
