package com.owl.core;

import com.owl.core.llm.LLMClient;
import com.owl.core.llm.LLMAgentResponse;
import com.owl.core.skills.SkillInstanceDefineRepo;
import lombok.AccessLevel;
import lombok.Getter;



/**
 * OWL Chat Agent 核心管理类
 * <p>
 * 获取一个ChatAgent实例 需要:
 * - LLMClient: 设置大模型数据
 * - Chat Agent Skill: 技能, 来描述对话的工作流程, 基本准则, 智能体的档案, 用户的档案, 可以使用工具的描述, 第一次创建的引导
 * - 所属会话ID: 隔离不同的历史, 为空会生一个新的会话ID, 第一次使用是激活会话的引导
 * - 所属用户ID: 隔离不同的用户, 为空使用默认用户ID
 *
 * <p>
 * 智能体
 * - 智能体定义: 在创建智能体定义时, 例如当前是 聊天智能体, 该域的数据记录不会随着对话而改变, 但是可以通过设定改变
 * - 会话: 在创建会话是创建这个域, 该域包含了, 对话的总结以及对话历史 (Session summary, Session history)
 * - 用户: 在创建用户档案时创建这个域, 该域包含了, 用户的档案信息 (User profile), 这里的用户信息只是 智能体对用户的总结和描述, 当然如果是面对单个用户, 则有一个系统级别的用户数据档案
 *
 * <p>
 * 系统用户档案:
 * - 用户档案: 由用户自行填写
 * - 用户的记忆索引: 智能体通过和用户对话自动更新
 * - 用户的画像描述: 智能体通过和用户对话自动更新
 * - 历史的梦: 手动或自动激活做梦, 通过做梦智能体采集近期的对话对用户做阶段性总结
 *
 * <p>
 * 流程:
 * - 获取一个智能体实例
 * - 参数, LLMClient, Chat Agent Skill(内置), 会话ID, 用户ID
 * - 加载到当前会话(或创建 - 创建会有创建的引导)
 * - 得到用户输入信息
 * - 智能体处理用户输入
 *
 * @author OWL Team
 * @version 1.0
 * @since 2026-04-16
 */
public class ChatAgent {


    private final LLMClient llmClient;
    private final SkillInstanceDefineRepo agentSkillRepo;
    @Getter(AccessLevel.PACKAGE)
    private final String sessionId;
    @Getter(AccessLevel.PACKAGE)
    private final String userId;

    /**
     * 构造 ChatAgent 实例
     *
     * @param llmClient      LLM 客户端，用于与大模型通信
     * @param agentSkillRepo 智能体技能仓库，包含工作流程、行为准则等配置
     * @param sessionId      会话 ID，用于隔离不同的对话历史。如果为 null，将自动生成
     * @param userId         用户 ID，用于隔离不同用户的配置和记忆。如果为 null，使用默认用户
     */
    private ChatAgent(LLMClient llmClient, SkillInstanceDefineRepo agentSkillRepo, String sessionId, String userId) {
        this.llmClient = llmClient;
        this.agentSkillRepo = agentSkillRepo;
        this.sessionId = sessionId != null ? sessionId : generateSessionId();
        this.userId = userId != null ? userId : "default_user";
    }

    /**
     * 对话流程
     * <p>
     * 处理用户输入消息，调用 LLM 生成回复。
     * 自动加载用户的配置信息（行为准则、用户档案、智能体档案等）作为上下文。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>从技能仓库加载用户配置（SOUL, USER, IDENTITY, TOOLS, HEARTBEAT）</li>
     *   <li>将配置信息作为系统消息传递给 LLM</li>
     *   <li>构建 ChatRequest 对象并调用 LLM 客户端</li>
     *   <li>返回 AI 的回复内容</li>
     * </ol>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * ChatAgent agent = ChatAgent.builder(llmClient)
     *     .agentSkillRepo(skillRepo)
     *     .userId("user-123")
     *     .build();
     * 
     * String response = agent.chat("你好，请介绍一下自己");
     * System.out.println(response);
     * }</pre>
     *
     * @param userMessage 用户输入的消息内容
     * @return AI 生成的回复内容
     */
    public String chat(String userMessage) {
        // 从技能仓库加载用户配置
        String soul = agentSkillRepo.findAgentSkillsByUserIdAndType(userId, "SOUL");
        String user = agentSkillRepo.findAgentSkillsByUserIdAndType(userId, "USER");
        String identity = agentSkillRepo.findAgentSkillsByUserIdAndType(userId, "IDENTITY");
        String tools = agentSkillRepo.findAgentSkillsByUserIdAndType(userId, "TOOLS");
        String heartbeat = agentSkillRepo.findAgentSkillsByUserIdAndType(userId, "HEARTBEAT");

        // 调用 LLM 客户端生成回复
        try {
            // 使用链式调用构建请求
            LLMClient.ChatRequestBuilder requestBuilder = llmClient.chat(userMessage);

            // 添加系统消息
            if (soul != null && !soul.isEmpty()) {
                requestBuilder.systemMessage(soul);
            }
            if (user != null && !user.isEmpty()) {
                requestBuilder.systemMessage(user);
            }
            if (identity != null && !identity.isEmpty()) {
                requestBuilder.systemMessage(identity);
            }
            if (tools != null && !tools.isEmpty()) {
                requestBuilder.systemMessage(tools);
            }
            if (heartbeat != null && !heartbeat.isEmpty()) {
                requestBuilder.systemMessage(heartbeat);
            }

            // 调用 LLM
            LLMAgentResponse response = requestBuilder.call();

            // 提取并返回回复内容
            if (response != null && response.isSuccess() && response.content() != null) {
                return response.content();
            }

            // 如果响应为空或失败，返回空字符串
            return "";

        } catch (Exception e) {
            // 记录错误日志并返回友好的错误提示
            System.err.println("LLM 调用失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，我遇到了一些问题，无法回复您的消息。请稍后重试。";
        }
    }


    /**
     * 创建 Builder 实例
     * <p>
     * 使用链式调用构建 ChatAgent 实例，提供更灵活的配置方式。
     * LLM 客户端在创建 Builder 时指定，其他参数通过链式调用设置。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 1. 基本用法 - 只指定技能仓库
     * ChatAgent agent = ChatAgent.builder(llmClient)
     *     .agentSkillRepo(skillRepo)
     *     .build();
     *
     * // 2. 指定会话 ID 和用户 ID
     * ChatAgent agent = ChatAgent.builder(llmClient)
     *     .agentSkillRepo(skillRepo)
     *     .sessionId("session-123")
     *     .userId("user-456")
     *     .build();
     *
     * // 3. 只指定用户 ID（会话 ID 自动生成）
     * ChatAgent agent = ChatAgent.builder(llmClient)
     *     .agentSkillRepo(skillRepo)
     *     .userId("user-789")
     *     .build();
     * }</pre>
     *
     * @param llmClient LLM 客户端实例（必填）
     * @return Builder 实例
     */
    public static Builder builder(LLMClient llmClient) {
        return new Builder(llmClient);
    }

    /**
     * 生成唯一的会话 ID
     * <p>
     * 当未提供会话 ID 时，自动生成一个基于时间戳和随机数的唯一标识。
     * </p>
     *
     * @return 生成的会话 ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }


    // ==================== Builder 模式 ====================

    /**
     * ChatAgent 构建器
     * <p>
     * 提供链式调用的方式来构建 ChatAgent 实例，使代码更清晰、更易读。
     * LLM 客户端在构造时指定，其他参数可选配置。
     * </p>
     *
     * <h3>必填参数：</h3>
     * <ul>
     *   <li><b>llmClient</b> - 在构造 Builder 时指定，用于与大模型通信</li>
     *   <li><b>agentSkillRepo</b> - 通过 agentSkillRepo() 方法设置，包含配置信息</li>
     * </ul>
     *
     * <h3>可选参数：</h3>
     * <ul>
     *   <li><b>sessionId</b> - 会话 ID，不设置则自动生成</li>
     *   <li><b>userId</b> - 用户 ID，不设置则使用 "default_user"</li>
     * </ul>
     */
    public static class Builder {
        private final LLMClient llmClient;
        private SkillInstanceDefineRepo agentSkillRepo;
        private String sessionId;
        private String userId;

        /**
         * 构造 Builder 实例
         *
         * @param llmClient LLM 客户端实例（必填）
         */
        public Builder(LLMClient llmClient) {
            this.llmClient = llmClient;
        }

        /**
         * 设置智能体技能仓库（必填）
         *
         * @param agentSkillRepo 技能仓库实例
         * @return Builder 实例，支持链式调用
         */
        public Builder agentSkillRepo(SkillInstanceDefineRepo agentSkillRepo) {
            this.agentSkillRepo = agentSkillRepo;
            return this;
        }

        /**
         * 设置会话 ID（可选）
         * <p>
         * 如果不设置，将自动生成一个唯一的会话 ID。
         * 会话 ID 用于隔离不同的对话历史。
         * </p>
         *
         * @param sessionId 会话 ID
         * @return Builder 实例，支持链式调用
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * 设置用户 ID（可选）
         * <p>
         * 如果不设置，将使用默认值 "default_user"。
         * 用户 ID 用于隔离不同用户的配置和记忆。
         * </p>
         *
         * @param userId 用户 ID
         * @return Builder 实例，支持链式调用
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * 构建 ChatAgent 实例
         * <p>
         * 验证必填参数后创建 ChatAgent 实例。
         * 如果 agentSkillRepo 未设置，将抛出异常。
         * </p>
         *
         * @return 构建好的 ChatAgent 实例
         * @throws IllegalStateException 如果 agentSkillRepo 未设置
         */
        public ChatAgent build() {
            // 验证必填参数
            if (agentSkillRepo == null) {
                throw new IllegalStateException("agentSkillRepo is required");
            }

            return new ChatAgent(llmClient, agentSkillRepo, sessionId, userId);
        }
    }


}
