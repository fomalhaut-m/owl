package com.owl.agent;

import com.owl.core.llm.*;
import com.owl.core.skills.DefaultPrompts;
import com.owl.core.skills.tools.AgentTools;
import com.owl.core.skills.AgentSkillRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    static Map<String, String> userConfig = new ConcurrentHashMap<>();
    static Map<String, Integer> userConfigSetCount = new ConcurrentHashMap<>();
    static Map<String, Integer> userConfigGetCount = new ConcurrentHashMap<>();

    private static final String API_KEY = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";

    static {
        userConfig.put("USER", DefaultPrompts.USER_DEFAULT);
        userConfig.put("IDENTITY", DefaultPrompts.IDENTITY_DEFAULT);
        userConfig.put("SOUL", DefaultPrompts.SOUL_DEFAULT);
        userConfig.put("TOOLS", DefaultPrompts.TOOLS_DEFAULT);
        userConfig.put("HEARTBEAT", DefaultPrompts.HEARTBEAT_DEFAULT);
    }

    /**
     * 创建火山引擎配置的 LLMClient
     */
    private LLMClient createHuoshanClient() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        return LLMClient.create(config);
    }

    @Test
    @DisplayName("测试Agent chat - 基本对话")
    void testChatBasic() {
        LLMClient client = createHuoshanClient();
        AgentTools agentTools = getSettingUserAgentTools();

        LLMAgentResponse response1 = client.chat(DefaultPrompts.BOOTSTRAP_DEFAULT)
                .systemMessage(userConfig.get("USER"))
                .systemMessage(userConfig.get("IDENTITY"))
                .systemMessage(userConfig.get("SOUL"))
                .systemMessage(userConfig.get("TOOLS"))
                .systemMessage(userConfig.get("HEARTBEAT"))
                .tool(agentTools)
                .call();

        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        String content1 = response1.content();
        assertNotNull(content1);
        assertFalse(content1.isEmpty());
        System.out.println("AI 回复: " + content1);
        System.out.println();

        String userMessage = "我是小雷，你是小智, 小智你是一个短剧专家!";

        LLMAgentResponse response2 = client.chat(userMessage)
                .systemMessage(userConfig.get("USER"))
                .systemMessage(userConfig.get("IDENTITY"))
                .systemMessage(userConfig.get("SOUL"))
                .systemMessage(userConfig.get("TOOLS"))
                .systemMessage(userConfig.get("HEARTBEAT"))
                .assistantMessage(content1)
                .tool(agentTools)
                .call();

        assertNotNull(response2);
        assertTrue(response2.isSuccess());
        String content2 = response2.content();
        assertNotNull(content2);
        assertFalse(content2.isEmpty());
        System.out.println("AI 回复: " + content2);
        System.out.println();

        String followUpMessage = "好的，小智。你能帮我做什么？";

        LLMAgentResponse response3 = client.chat(followUpMessage)
                .systemMessage(userConfig.get("USER"))
                .systemMessage(userConfig.get("IDENTITY"))
                .systemMessage(userConfig.get("SOUL"))
                .systemMessage(userConfig.get("TOOLS"))
                .systemMessage(userConfig.get("HEARTBEAT"))
                .assistantMessage(content1)
                .assistantMessage(content2)
                .tool(agentTools)
                .call();

        assertNotNull(response3);
        assertTrue(response3.isSuccess());
        String content3 = response3.content();
        assertNotNull(content3);
        assertFalse(content3.isEmpty());
        System.out.println("AI 回复: " + content3);

        System.out.println("========== 结束 ==========");
        System.out.println("检查是否有写入配置");

        System.out.println("USER.md: " + userConfig.get("USER"));
        System.out.println("IDENTITY.md: " + userConfig.get("IDENTITY"));
        System.out.println("SOUL.md: " + userConfig.get("SOUL"));
        System.out.println("TOOLS.md: " + userConfig.get("TOOLS"));
        System.out.println("HEARTBEAT.md: " + userConfig.get("HEARTBEAT"));

        System.out.println("读入次数 " + userConfigGetCount);
        System.out.println("写入次数 " + userConfigSetCount);
    }

    private static AgentTools getSettingUserAgentTools() {
        return new AgentTools(new AgentSkillRepo() {

            @Override
            public String findAgentSkillsByUserIdAndType(String userId, String type) {
                userConfigGetCount.put(type, userConfigGetCount.getOrDefault(type, 0) + 1);
                return userConfig.get(type);
            }

            @Override
            public void saveAgentSkills(String userId, String type, String content) {
                userConfigSetCount.put(type, userConfigSetCount.getOrDefault(type, 0) + 1);
                userConfig.put(type, content);
            }
        });
    }
}
