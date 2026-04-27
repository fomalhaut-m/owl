package com.owl.core.llm;

import com.owl.core.tools.TimeTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LLMClient 测试 - Builder 模式")
class LLMClientTest {

    private static final String API_KEY = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";

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

    // ==================== 参数校验测试 ====================

    @Test
    @DisplayName("测试 LLMConfig 为空时抛出异常")
    void testCreateWithNullConfig() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(null)
        );
        assertEquals("LLMConfig 不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试 llmPlatform 为空时抛出异常")
    void testCreateWithNullPlatform() {
        LLMConfig config = new LLMConfig();
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("LLM 平台不能为空"));
    }

    @Test
    @DisplayName("测试 apiKey 为空时抛出异常")
    void testCreateWithBlankApiKey() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey("");
        config.setModel("doubao-seed-2.0-lite");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("API Key 不能为空"));
    }

    @Test
    @DisplayName("测试 model 为空时抛出异常")
    void testCreateWithBlankModel() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("模型名称不能为空"));
    }

    @Test
    @DisplayName("测试 temperature 超出范围时抛出异常")
    void testCreateWithInvalidTemperature() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");
        config.setTemperature(3.0); // 超出 0-2 范围

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("温度参数不能大于 2"));
    }

    @Test
    @DisplayName("测试 maxTokens 小于等于 0 时抛出异常")
    void testCreateWithInvalidMaxTokens() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");
        config.setMaxTokens(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("最大 Token 数必须大于 0"));
    }

    @Test
    @DisplayName("测试 userMessage 为空时抛出异常")
    void testChatWithBlankUserMessage() {
        LLMClient client = createHuoshanClient();

        assertThrows(
                IllegalArgumentException.class,
                () -> client.chat("")
        );
    }

    @Test
    @DisplayName("测试 userMessage 为 null 时抛出异常")
    void testChatWithNullUserMessage() {
        LLMClient client = createHuoshanClient();

        assertThrows(
                IllegalArgumentException.class,
                () -> client.chat(null)
        );
    }

    // ==================== 基础功能测试 ====================

    @Test
    @DisplayName("测试简单对话")
    void testSimpleChat() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("你好").call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.content());
        assertFalse(response.content().isEmpty());
        System.out.println("简单对话响应: " + response.content());
    }

    @Test
    @DisplayName("测试带系统消息的对话")
    void testChatWithSystemMessage() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("请介绍一下自己")
                .systemMessage("你是一个乐于助人的 AI 助手")
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.content());
        System.out.println("带系统消息响应: " + response.content());
    }

    @Test
    @DisplayName("测试多系统消息")
    void testChatWithMultipleSystemMessages() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("写一首诗")
                .systemMessage("你是一个诗人")
                .systemMessage("每次回复不超过50字")
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("多系统消息响应: " + response.content());
    }

    @Test
    @DisplayName("测试带历史消息的对话")
    void testChatWithHistory() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("继续")
                .systemMessage("你是一个诗人")
                .assistantMessage("春风又绿江南岸，明月何时照我还")
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("带历史消息响应: " + response.content());
    }

    @Test
    @DisplayName("测试带工具调用的对话")
    void testChatWithTool() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("现在几点？")
                .tool(new TimeTools())
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("工具调用响应: " + response.content());
    }

    @Test
    @DisplayName("测试批量添加工具")
    void testChatWithMultipleTools() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("查询时间")
                .tool(new TimeTools())
                .tools(new TimeTools(), new TimeTools())
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("多工具响应: " + response.content());
    }

    // ==================== 运行时参数测试 ====================

    @Test
    @DisplayName("测试运行时覆盖 model")
    void testRuntimeModel() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("你好")
                .model("doubao-seed-2.0-pro")
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("运行时 model 响应: " + response.content());
    }

    @Test
    @DisplayName("测试运行时设置 temperature")
    void testRuntimeTemperature() {
        LLMClient client = createHuoshanClient();

        // 低温 - 确定性输出
        LLMAgentResponse lowTemp = client.chat("1+1等于几？")
                .temperature(0.1)
                .call();
        assertTrue(lowTemp.isSuccess());

        // 高温 - 创意输出
        LLMAgentResponse highTemp = client.chat("写一个故事开头")
                .temperature(1.0)
                .call();
        assertTrue(highTemp.isSuccess());

        System.out.println("低温响应: " + lowTemp.content());
        System.out.println("高温响应: " + highTemp.content());
    }

    @Test
    @DisplayName("测试运行时设置 maxTokens")
    void testRuntimeMaxTokens() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("详细介绍一下春天")
                .maxTokens(100) // 限制输出长度
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("限制 maxTokens 响应: " + response.content());
    }

    @Test
    @DisplayName("测试组合运行时参数")
    void testRuntimeCombinedParams() {
        LLMClient client = createHuoshanClient();

        LLMAgentResponse response = client.chat("写一首诗")
                .model("doubao-seed-2.0-pro")
                .temperature(0.8)
                .maxTokens(200)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("组合参数响应: " + response.content());
    }

    // ==================== 用户元数据测试 ====================

    @Test
    @DisplayName("测试用户元数据传递")
    void testUserMetadata() {
        LLMClient client = createHuoshanClient();

        UserMetadata metadata = UserMetadata.builder()
                .userId("user-123")
                .sessionId("session-456")
                .build();

        LLMAgentResponse response = client.chat("你好")
                .userMetadata(metadata)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("带用户元数据响应: " + response.content());
    }

    // ==================== 流式调用测试 ====================

    @Test
    @DisplayName("测试流式调用基本对话")
    void testStreamBasic() {
        LLMClient client = createHuoshanClient();

        StringBuilder fullResponse = new StringBuilder();
        client.chat("写一个春天的故事")
                .callStream()
                .doOnNext(response -> {
                    if (response.content() != null) {
                        fullResponse.append(response.content());
                        System.out.print(response.content());
                    }
                })
                .blockLast();

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\n流式完整响应: " + fullResponse);
    }

    @Test
    @DisplayName("测试流式调用带工具")
    void testStreamWithTool() {
        LLMClient client = createHuoshanClient();

        StringBuilder fullResponse = new StringBuilder();
        client.chat("现在几点？")
                .tool(new TimeTools())
                .callStream()
                .doOnNext(response -> {
                    if (response.content() != null) {
                        fullResponse.append(response.content());
                        System.out.print(response.content());
                    }
                })
                .blockLast();

        assertFalse(fullResponse.toString().isEmpty());
        System.out.println("\n流式工具响应: " + fullResponse);
    }

    // ==================== 代理配置测试 ====================

    @Test
    @DisplayName("测试启用代理时 host 为空抛出异常")
    void testProxyEnabledWithoutHost() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");

        LLMConfig.ProxyDefinition proxy = new LLMConfig.ProxyDefinition();
        proxy.setProxyEnabled(true);
        proxy.setHost(""); // 空 host
        proxy.setPort(7890);
        config.setProxyDefinition(proxy);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LLMClient.create(config)
        );
        assertTrue(exception.getMessage().contains("代理主机地址不能为空"));
    }

    @Test
    @DisplayName("测试禁用代理时正常创建")
    void testProxyDisabled() {
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("doubao-seed-2.0-lite");

        LLMConfig.ProxyDefinition proxy = new LLMConfig.ProxyDefinition();
        proxy.setProxyEnabled(false);
        proxy.setHost(""); // 即使为空也不报错
        proxy.setPort(0);
        config.setProxyDefinition(proxy);

        // 不应抛出异常
        LLMClient client = LLMClient.create(config);
        assertNotNull(client);
    }

    // ==================== 完整流程测试 ====================

    @Test
    @DisplayName("测试完整对话流程")
    void testCompleteConversationFlow() {
        LLMClient client = createHuoshanClient();

        // 第一轮：简单问候
        LLMAgentResponse first = client.chat("你好，你是谁？")
                .systemMessage("你是一个专业的技术顾问")
                .call();
        assertTrue(first.isSuccess());
        System.out.println("第一轮: " + first.content());

        // 第二轮：继续对话（带历史）
        LLMAgentResponse second = client.chat("继续说说你的专长")
                .systemMessage("你是一个专业的技术顾问")
                .assistantMessage(first.content())
                .call();
        assertTrue(second.isSuccess());
        System.out.println("第二轮: " + second.content());

        // 第三轮：工具调用
        LLMAgentResponse third = client.chat("现在几点？")
                .assistantMessage(second.content())
                .tool(new TimeTools())
                .call();
        assertTrue(third.isSuccess());
        System.out.println("第三轮: " + third.content());
    }
}
