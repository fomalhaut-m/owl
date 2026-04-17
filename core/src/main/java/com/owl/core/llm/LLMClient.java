package com.owl.core.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.List;

@Slf4j
public class LLMClient {

    private final ChatClient chatClient;
    private final String[] defaultToolNames = {"", "", ""};

    public static LLMClient create(LLMConfig llmConfig) {
        return new LLMClient(llmConfig);
    }

    public static LLMClient create(LLMConfig llmConfig, ChatClient chatClient) {
        return new LLMClient(llmConfig);
    }

    private LLMClient(LLMConfig llmConfig) {
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(llmConfig.getApiKey())
                        .baseUrl(llmConfig.getLlmPlatform().getBaseUrl())
                        .completionsPath(llmConfig.getLlmPlatform().getChatPath())
                        .restClientBuilder(restClientBuilder(llmConfig.getProxyDefinition()))
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(llmConfig.getModel())
                        .temperature(llmConfig.getTemperature())
                        .maxTokens(llmConfig.getMaxTokens())
                        .build())
                .build();

        this.chatClient = ChatClient.builder(chatModel)
//                .defaultToolNames(defaultToolNames)
                .build();
    }

    /**
     * 同步调用 LLM，获取完整响应
     * <p>
     * 支持多轮对话上下文和工具调用，适用于需要等待完整结果的场景。
     * </p>
     *
     * @param userMessage   用户当前输入消息
     * @param messages      历史消息列表，包含：
     *                      <ul>
     *                      <li>SystemMessage：设定 AI 角色和系统指令</li>
     *                      <li>UserMessage：用户输入</li>
     *                      <li>AssistantMessage：AI 回复（可能含工具请求）</li>
     *                      <li>ToolResponseMessage：工具执行结果（返回给 AI）</li>
     *                      </ul>
     * @param toolCallbacks 工具回调数组，用于支持函数调用能力
     * @return ChatClientResponse 包含 AI 的完整响应内容、元数据等
     */
    public ChatClientResponse chat(String userMessage, List<Message> messages, ToolCallback... toolCallbacks) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt(userMessage);
        if (messages != null && !messages.isEmpty()) {
            spec.messages(messages);
        }
        if (toolCallbacks != null && toolCallbacks.length > 0) {
            spec.toolCallbacks(toolCallbacks);
        }
        return spec.call().chatClientResponse();
    }

    /**
     * 流式调用 LLM，实时获取响应片段
     * <p>
     * 支持多轮对话上下文和工具调用，适用于需要实时展示 AI 回复的场景（如聊天界面打字机效果）。
     * 返回 Flux 响应式流，可以逐块接收 AI 生成的内容。
     * </p>
     *
     * @param userMessage   用户当前输入消息
     * @param messages      历史消息列表，包含：
     *                      <ul>
     *                      <li>SystemMessage：设定 AI 角色和系统指令</li>
     *                      <li>UserMessage：用户输入</li>
     *                      <li>AssistantMessage：AI 回复（可能含工具请求）</li>
     *                      <li>ToolResponseMessage：工具执行结果（返回给 AI）</li>
     *                      </ul>
     * @param toolCallbacks 工具回调数组，用于支持函数调用能力
     * @return Flux&lt;ChatClientResponse&gt; 响应式流，逐个发射 AI 响应片段
     */
    public Flux<ChatClientResponse> chatStream(String userMessage, List<Message> messages, ToolCallback... toolCallbacks) {
        return chatClient.prompt(userMessage).messages(messages).toolCallbacks(toolCallbacks).stream().chatClientResponse();
    }


    private RestClient.Builder restClientBuilder(LLMConfig.ProxyDefinition proxyDefinition) {
        java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))  // 连接超时30秒
                .version(java.net.http.HttpClient.Version.HTTP_2);  // 使用HTTP/2
        if (proxyDefinition != null && proxyDefinition.isProxyEnabled()) {
            log.debug("使用代理配置, host:{}, port:{}", proxyDefinition.getHost(), proxyDefinition.getPort());
            httpClientBuilder.proxy(ProxySelector.of(InetSocketAddress.createUnresolved(proxyDefinition.getHost(), proxyDefinition.getPort())));
        }

        java.net.http.HttpClient httpClient = httpClientBuilder.build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    log.trace("HTTP请求拦截 - 请求URL:{}, 方法:{}, headers:{}", request.getURI(), request.getMethod(), request.getHeaders());
                    return execution.execute(request, body);
                });
    }
}
