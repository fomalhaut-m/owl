package com.owl.agent;

import com.owl.core.llm.*;
import com.owl.core.skills.DefaultPrompts;
import com.owl.core.skills.tools.AgentTools;
import com.owl.core.skills.AgentSkillRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    static Map<String, String> userConfig = new ConcurrentHashMap<>();
    static Map<String, Integer> userConfigSetCount = new ConcurrentHashMap<>();
    static Map<String, Integer> userConfigGetCount = new ConcurrentHashMap<>();

    String apiKey = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";

    static {
        userConfig.put("USER", DefaultPrompts.USER_DEFAULT);
        userConfig.put("IDENTITY", DefaultPrompts.IDENTITY_DEFAULT);
        userConfig.put("SOUL", DefaultPrompts.SOUL_DEFAULT);
        userConfig.put("TOOLS", DefaultPrompts.TOOLS_DEFAULT);
        userConfig.put("HEARTBEAT", DefaultPrompts.HEARTBEAT_DEFAULT);
    }

    /**
     * 创建火山引擎配置的 LLMClient（带默认工具）
     */
    private LLMClient createHuoshanClient() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        
        // 创建默认工具组件
        AgentTools settingUserAgentTools = getSettingUserAgentTools();
        return LLMClient.create(config, Arrays.asList(settingUserAgentTools));
    }

    @Test
    @DisplayName("测试Agent chat - 基本对话")
    void testChatBasic() {
        LLMClient client = createHuoshanClient();

        // 构建系统消息列表
        List<Message> systemMessages = List.of(
                new AssistantMessage(userConfig.get("USER")),
                new AssistantMessage(userConfig.get("IDENTITY")),
                new AssistantMessage(userConfig.get("SOUL")),
                new AssistantMessage(userConfig.get("TOOLS")),
                new AssistantMessage(userConfig.get("HEARTBEAT"))
        );

        // 第一轮对话：发送 BOOTSTRAP 引导脚本
        System.out.println("========== 第一轮：发送引导脚本 ==========");
        LLMChatRequest request1 = LLMChatRequest.of(DefaultPrompts.BOOTSTRAP_DEFAULT, systemMessages);
        LLMAgentResponse response1 = client.chat(request1);

        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        String content1 = response1.content();
        assertNotNull(content1);
        assertFalse(content1.isEmpty());
        System.out.println("AI 回复: " + content1);
        System.out.println();

        // 第二轮对话：告诉 AI 身份信息
        System.out.println("========== 第二轮：告诉 AI 身份信息 ==========");
        String userMessage = "我是小雷，你是小智, 小智你是一个短剧专家!";
        System.out.println("用户说: " + userMessage);

        // 构建包含上一轮对话的历史消息
        List<Message> history2 = Arrays.asList(
                new AssistantMessage(userConfig.get("USER")),
                new AssistantMessage(userConfig.get("IDENTITY")),
                new AssistantMessage(userConfig.get("SOUL")),
                new AssistantMessage(userConfig.get("TOOLS")),
                new AssistantMessage(userConfig.get("HEARTBEAT")),
                new AssistantMessage(content1)  // 添加上一轮 AI 的回复
        );
        
        LLMChatRequest request2 = LLMChatRequest.of(userMessage, history2);
        LLMAgentResponse response2 = client.chat(request2);

        assertNotNull(response2);
        assertTrue(response2.isSuccess());
        String content2 = response2.content();
        assertNotNull(content2);
        assertFalse(content2.isEmpty());
        System.out.println("AI 回复: " + content2);
        System.out.println();

        // 第三轮对话：继续询问 AI
        System.out.println("========== 第三轮：继续与 AI 对话 ==========");
        String followUpMessage = "好的，小智。你能帮我做什么？";
        System.out.println("用户说: " + followUpMessage);

        // 构建包含前两轮对话的历史消息
        List<Message> history3 = Arrays.asList(
                new AssistantMessage(userConfig.get("USER")),
                new AssistantMessage(userConfig.get("IDENTITY")),
                new AssistantMessage(userConfig.get("SOUL")),
                new AssistantMessage(userConfig.get("TOOLS")),
                new AssistantMessage(userConfig.get("HEARTBEAT")),
                new AssistantMessage(content1),  // 第一轮 AI 回复
                new AssistantMessage(content2)   // 第二轮 AI 回复
        );
        
        LLMChatRequest request3 = LLMChatRequest.of(followUpMessage, history3);
        LLMAgentResponse response3 = client.chat(request3);

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
            public String getAgentSkills(String userId, String type) {
                userConfigGetCount.put(type, userConfigGetCount.getOrDefault(type, 0) + 1);
                return userConfig.get(type);
            }

            @Override
            public String setAgentSkills(String userId, String type, String content) {
                userConfigSetCount.put(type, userConfigSetCount.getOrDefault(type, 0) + 1);
                return userConfig.put(type, content);
            }
        });
    }
}
