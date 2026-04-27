package com.owl.core;

import com.owl.core.llm.LLMClient;
import com.owl.core.llm.LLMConfig;
import com.owl.core.llm.LLMPlatformEnum;
import com.owl.core.skills.SkillInstanceDefineRepo;
import com.owl.core.skills.DefaultPrompts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatAgent 单元测试
 * <p>
 * 测试 ChatAgent 的核心功能，包括：
 * - 实例创建
 * - 对话功能
 * - 会话 ID 生成
 * - 配置加载
 * </p>
 *
 * @author OWL Team
 * @version 1.0
 * @since 2026-04-16
 */
@DisplayName("ChatAgent 测试")
class ChatAgentTest {

    private static final String API_KEY = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";
    private LLMClient llmClient;
    private SkillInstanceDefineRepo skillRepo;
    private Map<String, String> userConfig;

    /**
     * 测试前初始化
     */
    @BeforeEach
    void setUp() {
        // 创建 LLM 客户端（使用新的 Builder 模式）
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        llmClient = LLMClient.create(config);

        // 初始化用户配置
        userConfig = new ConcurrentHashMap<>();
        userConfig.put("USER", DefaultPrompts.USER_DEFAULT);
        userConfig.put("IDENTITY", DefaultPrompts.IDENTITY_DEFAULT);
        userConfig.put("SOUL", DefaultPrompts.SOUL_DEFAULT);
        userConfig.put("TOOLS", DefaultPrompts.TOOLS_DEFAULT);
        userConfig.put("HEARTBEAT", DefaultPrompts.HEARTBEAT_DEFAULT);

        // 创建技能仓库
        skillRepo = createSkillRepo();
    }

    /**
     * 创建技能仓库
     */
    private SkillInstanceDefineRepo createSkillRepo() {
        return new SkillInstanceDefineRepo() {
            @Override
            public String findAgentSkillsByUserIdAndType(String userId, String type) {
                return userConfig.get(type);
            }

            @Override
            public void saveAgentSkills(String userId, String type, String content) {
                userConfig.put(type, content);
            }
        };
    }

    @Test
    @DisplayName("测试 ChatAgent 创建 - 基本用法")
    void testCreateBasic() {
        ChatAgent agent = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .build();

        assertNotNull(agent);
        assertNotNull(agent.getSessionId());
        assertEquals("default_user", agent.getUserId());
    }

    @Test
    @DisplayName("测试 ChatAgent 创建 - 指定会话 ID 和用户 ID")
    void testCreateWithIds() {
        String sessionId = "test-session-123";
        String userId = "test-user-456";

        ChatAgent agent = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .sessionId(sessionId)
                .userId(userId)
                .build();

        assertNotNull(agent);
        assertEquals(sessionId, agent.getSessionId());
        assertEquals(userId, agent.getUserId());
    }

    @Test
    @DisplayName("测试 ChatAgent 创建 - 自动生成会话 ID")
    void testCreateAutoSessionId() {
        ChatAgent agent1 = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("user-1")
                .build();

        ChatAgent agent2 = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("user-1")
                .build();

        assertNotNull(agent1.getSessionId());
        assertNotNull(agent2.getSessionId());
        assertNotEquals(agent1.getSessionId(), agent2.getSessionId());
    }


    @Test
    @DisplayName("测试 chat - 基本对话")
    void testChatBasic() {
        ChatAgent agent = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("test-user")
                .build();

        String response = agent.chat("你好，请简单回复");

        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("AI 回复: " + response);
    }

    @Test
    @DisplayName("测试 chat - 多轮对话")
    void testChatMultiTurn() {
        ChatAgent agent = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("test-user")
                .build();

        // 第一轮对话
        String response1 = agent.chat("我是小雷，你是小智");
        assertNotNull(response1);
        assertFalse(response1.isEmpty());
        System.out.println("第一轮 AI 回复: " + response1);

        // 第二轮对话
        String response2 = agent.chat("好的，小智。你能帮我做什么？");
        assertNotNull(response2);
        assertFalse(response2.isEmpty());
        System.out.println("第二轮 AI 回复: " + response2);
    }

    @Test
    @DisplayName("测试 chat - 不同用户隔离")
    void testChatUserIsolation() {
        // 用户 A
        userConfig.put("USER", "# 用户 A\n\n名字：张三");
        ChatAgent agentA = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("user-a")
                .build();

        // 用户 B
        userConfig.put("USER", "# 用户 B\n\n名字：李四");
        ChatAgent agentB = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .userId("user-b")
                .build();

        // 验证两个用户的配置是隔离的
        String userAConfig = skillRepo.findAgentSkillsByUserIdAndType("user-a", "USER");
        String userBConfig = skillRepo.findAgentSkillsByUserIdAndType("user-b", "USER");

        assertNotEquals(userAConfig, userBConfig);
        assertTrue(userAConfig.contains("张三"));
        assertTrue(userBConfig.contains("李四"));
    }

    @Test
    @DisplayName("测试 Builder - 缺少必填参数抛出异常")
    void testBuilderMissingRequiredParam() {
        assertThrows(IllegalStateException.class, () -> {
            ChatAgent.builder(llmClient)
                    .build(); // 缺少 agentSkillRepo
        });
    }

    @Test
    @DisplayName("测试 Builder - 链式调用")
    void testBuilderChaining() {
        ChatAgent agent = ChatAgent.builder(llmClient)
                .agentSkillRepo(skillRepo)
                .sessionId("custom-session")
                .userId("custom-user")
                .build();

        assertNotNull(agent);
        assertEquals("custom-session", agent.getSessionId());
        assertEquals("custom-user", agent.getUserId());
    }
}
