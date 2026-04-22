package com.owl.core.llm;

import com.owl.core.skills.tools.TimeTools;
import com.owl.core.skills.tools.ToolComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LLMClient 测试")
class LLMClientTest {

    String apiKey = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";

    /**
     * 创建火山引擎配置的 LLMClient
     */
    private LLMClient createHuoshanClient() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        return LLMClient.create(config);
    }

    @Test
    @DisplayName("测试同步调用 chat - 基本对话")
    void testChatBasic() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat(
                "Hi, How are you?",
                Collections.emptyList()
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("同步响应: " + content);
    }

    @Test
    @DisplayName("测试同步调用 chat - 带历史消息")
    void testChatWithHistory() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat(
                "请介绍一下你自己",
                Collections.emptyList()
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("带历史的同步响应: " + content);
    }

    @Test
    @DisplayName("测试同步调用 chat - 使用工具")
    void testChatWithTools() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("Doubao-Seed-2.0-pro");
        config.setTemperature(0.2);
        LLMClient client = LLMClient.create(config);

        ToolComponent timeTools = new TimeTools();

        LLMAgentResponse response = client.chat(
                "当前系统时间是? 当前上海的, 纽约的时间是?",
                Collections.emptyList(),
                timeTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("工具调用响应: " + content);
    }

    @Test
    @DisplayName("测试流式调用 chatStream - 基本对话")
    void testChatStreamBasic() {
        LLMClient client = createHuoshanClient();

        Flux<LLMAgentResponse> responseFlux = client.chatStream(
                "请写一首关于春天的短诗",
                Collections.emptyList()
        );

        assertNotNull(responseFlux);

        // 收集流式响应
        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.content();
            if (chunk != null) {
                fullResponse.append(chunk);
                System.out.print(chunk); // 实时打印，模拟打字机效果
            }
        }).blockLast(); // 等待流完成

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\n完整流式响应: " + fullResponse);
    }

    @Test
    @DisplayName("测试流式调用 chatStream - 带历史消息")
    void testChatStreamWithHistory() {
        LLMClient client = createHuoshanClient();

        Flux<LLMAgentResponse> responseFlux = client.chatStream(
                "用三句话总结人工智能的发展",
                Collections.emptyList()
        );

        assertNotNull(responseFlux);

        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.content();
            if (chunk != null) {
                fullResponse.append(chunk);
                System.out.print(chunk);
            }
        }).blockLast();

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\n带历史的流式响应: " + fullResponse);
    }

    @Test
    @DisplayName("测试流式调用 chatStream - 使用工具")
    void testChatStreamWithTools() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("Doubao-Seed-2.0-pro");
        config.setTemperature(0.2);
        LLMClient client = LLMClient.create(config);

        Flux<LLMAgentResponse> responseFlux = client.chatStream(
                "现在北京时间几点？伦敦时间呢？",
                Collections.emptyList(),
                new TimeTools()
        );

        assertNotNull(responseFlux);

        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.content();
            if (chunk != null) {
                fullResponse.append(chunk);
                System.out.print(chunk);
            }
        }).blockLast();

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\n工具调用的流式响应: " + fullResponse);
    }

    @Test
    @DisplayName("测试不同平台配置 - MiniMax")
    void testDifferentPlatformMiniMax() {
        String miniMaxApiKey = "sk-cp-0cx8-H-KKo14uqdNurVEZFw_U2KRjadkIGl3c41wfSVge75_ZE-v9GJHhtRyxZD96_l2461T8bK8KjGSRWwUj21Uhs_M1waHOZCuTViL3Vlvn10jh4iFPL0";

        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.MINIMAX);
        config.setApiKey(miniMaxApiKey);
        config.setModel("MiniMax-M2.7");
        config.setTemperature(0.2);

        LLMClient client = LLMClient.create(config);

        LLMAgentResponse response = client.chat(
                "Hello, tell me a joke",
                Collections.emptyList()
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("MiniMax 响应: " + content);
    }

    @Test
    @DisplayName("测试 ChatRequest API - 基本用法")
    void testChatRequestBasic() {
        // 创建带默认工具的客户端
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        
        LLMClient client = LLMClient.create(config);

        // 使用 ChatRequest.of() 便捷方法
        LLMChatRequest request = LLMChatRequest.of("你好，请介绍一下自己");
        
        LLMAgentResponse response = client.chat(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("ChatRequest 基本用法: " + content);
    }

    @Test
    @DisplayName("测试 ChatRequest API - 完整用法")
    void testChatRequestFull() {
        // 创建带默认工具的客户端
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("Doubao-Seed-2.0-pro");
        config.setTemperature(0.2);
        
        LLMClient client = LLMClient.create(config, Collections.singletonList(new TimeTools()));

        // 使用 Builder 模式构建完整的请求
        UserMetadata metadata = UserMetadata.builder()
                .userId("user-123")
                .sessionId("session-456")
                .build();
                
        LLMChatRequest request = LLMChatRequest.builder()
                .userMessage("当前北京时间几点？")
                .messages(Collections.emptyList())
                .userMetadata(metadata)
                .build();
        
        LLMAgentResponse response = client.chat(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        String content = response.content();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("ChatRequest 完整用法: " + content);
    }

    @Test
    @DisplayName("测试 ChatRequest 流式 API")
    void testChatRequestStream() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(apiKey);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(0.2);
        
        LLMClient client = LLMClient.create(config);

        // 使用 ChatRequest 进行流式调用
        LLMChatRequest request = LLMChatRequest.of("写一首关于秋天的短诗");
        
        Flux<LLMAgentResponse> responseFlux = client.chatStream(request);

        assertNotNull(responseFlux);

        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.content();
            if (chunk != null) {
                fullResponse.append(chunk);
                System.out.print(chunk);
            }
        }).blockLast();

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\nChatRequest 流式响应: " + fullResponse);
    }
}
