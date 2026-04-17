package com.owl.core.llm;

import com.owl.core.tools.TimeTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

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

        ChatClientResponse response = client.chat(
                "Hi, How are you?",
                Collections.emptyList()
        );

        assertNotNull(response);
        String content = response.chatResponse().getResult().getOutput().getText();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("同步响应: " + content);
    }

    @Test
    @DisplayName("测试同步调用 chat - 带历史消息")
    void testChatWithHistory() {
        LLMClient client = createHuoshanClient();

        // 模拟简单的对话历史
        List<Message> messages = Collections.emptyList();

        ChatClientResponse response = client.chat(
                "请介绍一下你自己",
                messages
        );

        assertNotNull(response);
        String content = response.chatResponse().getResult().getOutput().getText();
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

        ToolCallback[] callbacks = ToolCallbacks.from(new TimeTools());

        ChatClientResponse response = client.chat(
                "当前系统时间是? 当前上海的, 纽约的时间是?",
                Collections.emptyList(),
                callbacks
        );

        assertNotNull(response);
        String content = response.chatResponse().getResult().getOutput().getText();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("工具调用响应: " + content);
    }

    @Test
    @DisplayName("测试流式调用 chatStream - 基本对话")
    void testChatStreamBasic() {
        LLMClient client = createHuoshanClient();

        Flux<ChatClientResponse> responseFlux = client.chatStream(
                "请写一首关于春天的短诗",
                Collections.emptyList()
        );

        assertNotNull(responseFlux);

        // 收集流式响应
        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.chatResponse().getResult().getOutput().getText();
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

        List<Message> messages = Collections.emptyList();

        Flux<ChatClientResponse> responseFlux = client.chatStream(
                "用三句话总结人工智能的发展",
                messages
        );

        assertNotNull(responseFlux);

        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.chatResponse().getResult().getOutput().getText();
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

        ToolCallback[] callbacks = ToolCallbacks.from(new TimeTools());

        Flux<ChatClientResponse> responseFlux = client.chatStream(
                "现在北京时间几点？伦敦时间呢？",
                Collections.emptyList(),
                callbacks
        );

        assertNotNull(responseFlux);

        StringBuilder fullResponse = new StringBuilder();
        responseFlux.doOnNext(response -> {
            String chunk = response.chatResponse().getResult().getOutput().getText();
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

        ChatClientResponse response = client.chat(
                "Hello, tell me a joke",
                Collections.emptyList()
        );

        assertNotNull(response);
        String content =response.chatResponse().getResult().getOutput().getText();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("MiniMax 响应: " + content);
    }
}
