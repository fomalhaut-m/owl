package com.owl.core.llm;

import com.owl.core.skills.tools.ToolComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * LLM 客户端封装类
 * <p>
 * 提供统一的 LLM 调用接口，支持同步和流式两种调用方式。
 * 基于 Spring AI 框架，兼容多种大模型平台（火山引擎、MiniMax、百度千帆等）。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>同步调用 - 等待完整响应后返回</li>
 *   <li>流式调用 - 实时接收响应片段，支持打字机效果</li>
 *   <li>工具调用 - 支持函数调用能力，扩展 AI 能力边界</li>
 *   <li>多轮对话 - 支持历史消息上下文管理</li>
 *   <li>代理配置 - 支持 HTTP 代理设置</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 创建配置
 * LLMConfig config = new LLMConfig();
 * config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
 * config.setApiKey("your-api-key");
 * config.setModel("doubao-seed-2.0-lite");
 * config.setTemperature(0.2);
 *
 * // 2. 创建客户端
 * LLMClient client = LLMClient.create(config);
 *
 * // 3. 同步调用
 * ChatClientResponse response = client.chat("你好", Collections.emptyList());
 * System.out.println(response.chatResponse().getResult().getOutput().getText());
 *
 * // 4. 流式调用
 * Flux<ChatClientResponse> stream = client.chatStream("写一首诗", Collections.emptyList());
 * stream.doOnNext(r -> System.out.print(r.chatResponse().getResult().getOutput().getText()))
 *       .blockLast();
 * }</pre>
 *
 * @author OWL Team
 * @version 1.0
 * @see LLMConfig
 * @see LLMPlatformEnum
 * @since 2026-04-16
 */
@Slf4j
public class LLMClient {

    /**
     * Spring AI ChatClient 实例
     * <p>
     * 底层聊天客户端，负责与大模型 API 进行实际通信。
     * 由构造函数初始化，线程安全，可复用。
     * </p>
     */
    private final ChatClient chatClient;

    /**
     * 默认工具组件列表
     * <p>
     * 通过构造函数注入的工具组件，在每次调用时自动使用。
     * 如果调用时传入了额外的工具组件，会与默认工具合并使用。
     * </p>
     */
    private final List<ToolComponent> defaultToolComponents;

    /**
     * 创建 LLMClient 实例（工厂方法）
     * <p>
     * 根据提供的配置创建一个新的 LLM 客户端实例。
     * 这是推荐的创建方式，使用静态工厂方法提高代码可读性。
     * </p>
     *
     * @param llmConfig LLM 配置对象，包含：
     *                  <ul>
     *                  <li>platform - LLM 平台枚举（火山引擎、MiniMax 等）</li>
     *                  <li>apiKey - API 密钥</li>
     *                  <li>model - 模型名称</li>
     *                  <li>temperature - 温度参数（0-2，越低越严谨）</li>
     *                  <li>maxTokens - 最大 Token 数</li>
     *                  <li>proxyDefinition - 代理配置（可选）</li>
     *                  </ul>
     * @return 新创建的 LLMClient 实例
     * @throws IllegalArgumentException 如果配置为空或必填字段缺失
     * @see LLMConfig
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * LLMConfig config = new LLMConfig();
     * config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
     * config.setApiKey("your-api-key");
     * config.setModel("doubao-seed-2.0-lite");
     * LLMClient client = LLMClient.create(config);
     * }</pre>
     */
    public static LLMClient create(LLMConfig llmConfig) {
        return new LLMClient(llmConfig, null);
    }

    /**
     * 创建 LLMClient 实例（带默认工具组件）
     * <p>
     * 根据提供的配置和默认工具组件创建一个新的 LLM 客户端实例。
     * 默认工具组件会在每次调用时自动使用。
     * </p>
     *
     * @param llmConfig            LLM 配置对象
     * @param defaultToolComponents 默认工具组件列表，可为 null
     * @return 新创建的 LLMClient 实例
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * List<ToolComponent> tools = Arrays.asList(new TimeTools(), new FileTools());
     * LLMClient client = LLMClient.create(config, tools);
     * }</pre>
     */
    public static LLMClient create(LLMConfig llmConfig, List<ToolComponent> defaultToolComponents) {
        return new LLMClient(llmConfig, defaultToolComponents);
    }

    /**
     * 私有构造函数，初始化 LLMClient
     * <p>
     * 根据配置创建 OpenAI 兼容的 ChatModel，并构建 ChatClient。
     * 配置包括：API 地址、密钥、模型、温度、Token 限制、代理等。
     * </p>
     *
     * <h3>初始化流程：</h3>
     * <ol>
     *   <li>从 LLMConfig 提取平台配置（baseUrl、chatPath）</li>
     *   <li>创建 OpenAiApi 实例，配置 API 密钥和地址</li>
     *   <li>配置 RestClient（含代理支持和请求拦截器）</li>
     *   <li>设置默认模型参数（model、temperature、maxTokens）</li>
     *   <li>构建 OpenAiChatModel</li>
     *   <li>使用 ChatClient.builder 包装 ChatModel</li>
     * </ol>
     *
     * @param llmConfig            LLM 配置对象，不能为 null
     * @param defaultToolComponents 默认工具组件列表，可为 null
     * @throws NullPointerException  如果 llmConfig 为 null
     * @throws IllegalStateException 如果 API 密钥或平台配置无效
     */
    private LLMClient(LLMConfig llmConfig, List<ToolComponent> defaultToolComponents) {
        // 构建 OpenAI 兼容的 ChatModel
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(llmConfig.getApiKey())                          // API 密钥
                        .baseUrl(llmConfig.getLlmPlatform().getBaseUrl())      // 平台基础 URL
                        .completionsPath(llmConfig.getLlmPlatform().getChatPath()) // 对话接口路径
                        .restClientBuilder(restClientBuilder(llmConfig.getProxyDefinition())) // REST 客户端配置
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(llmConfig.getModel())           // 模型名称
                        .temperature(llmConfig.getTemperature()) // 温度参数
                        .maxTokens(llmConfig.getMaxTokens())   // 最大 Token 数
                        .build())
                .build();

        // 构建 ChatClient
        this.chatClient = ChatClient.builder(chatModel)
//                .defaultToolNames(defaultToolNames)  // 暂不设置默认工具
                .build();

        // 保存默认工具组件
        this.defaultToolComponents = defaultToolComponents != null ? defaultToolComponents : Collections.emptyList();
    }

    // ==================== 基于 ChatRequest 的简化 API ====================

    /**
     * 同步调用 LLM（使用 ChatRequest）
     * <p>
     * 使用 ChatRequest 对象封装所有参数，简化方法签名。
     * 工具组件从构造方法注入，自动与请求合并使用。
     * </p>
     *
     * @param request 聊天请求对象，包含用户消息、历史消息、用户元数据
     * @return OwlChatResponse AI 响应
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 1. 简单用法
     * ChatRequest request = ChatRequest.of("你好");
     * OwlChatResponse response = client.chat(request);
     *
     * // 2. 完整用法
     * ChatRequest request = ChatRequest.builder()
     *     .userMessage("查询时间")
     *     .messages(history)
     *     .userMetadata(metadata)
     *     .build();
     * OwlChatResponse response = client.chat(request);
     * }</pre>
     */
    public LLMAgentResponse chat(LLMChatRequest request) {
        return chat(request.getUserMessage(), request.getMessages(), request.getUserMetadata(), defaultToolComponents);
    }

    /**
     * 流式调用 LLM（使用 ChatRequest）
     * <p>
     * 使用 ChatRequest 对象封装所有参数，简化方法签名。
     * 工具组件从构造方法注入，自动与请求合并使用。
     * </p>
     *
     * @param request 聊天请求对象，包含用户消息、历史消息、用户元数据
     * @return Flux&lt;OwlChatResponse&gt; 响应式流
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * ChatRequest request = ChatRequest.of("写一首诗");
     * client.chatStream(request)
     *     .doOnNext(response -> System.out.print(response.content()))
     *     .blockLast();
     * }</pre>
     */
    public Flux<LLMAgentResponse> chatStream(LLMChatRequest request) {
        return chatStream(request.getUserMessage(), request.getMessages(), request.getUserMetadata(), 
                         defaultToolComponents.toArray(new ToolComponent[0]));
    }

    // ==================== 原有 API（保持向后兼容） ====================

    public LLMAgentResponse chat(String userMessage, List<Message> messages, ToolComponent... toolComponents) {
        return chat(userMessage, messages, null, Arrays.asList(toolComponents));
    }

    /**
     * 同步调用 LLM（带用户元数据）
     *
     * @param userMessage    用户消息
     * @param messages       历史消息
     * @param userMetadata   用户元数据（可选）
     * @param toolComponents 工具组件
     * @return AI 响应
     */
    public LLMAgentResponse chat(String userMessage, List<Message> messages, UserMetadata userMetadata, ToolComponent... toolComponents) {
        return chat(userMessage, messages, userMetadata, Arrays.asList(toolComponents));
    }

    /**
     * 同步调用 LLM，获取完整响应
     * <p>
     * 发送用户消息到 LLM，等待完整响应后返回。适用于需要等待完整结果的场景，
     * 如后台任务处理、批量生成等。
     * </p>
     *
     * <h3>特性：</h3>
     * <ul>
     *   <li>✅ 支持多轮对话上下文（通过 messages 参数）</li>
     *   <li>✅ 支持工具调用（通过 toolComponents 参数）</li>
     *   <li>✅ 支持用户元数据传递（通过 userMetadata 参数）</li>
     *   <li>✅ 阻塞式调用，直到收到完整响应</li>
     *   <li>✅ 返回简化的响应对象，易于使用</li>
     * </ul>
     *
     * @param userMessage    用户当前输入消息，不能为 null 或空
     * @param messages       历史消息列表，用于维持对话上下文，可为 null 或空列表。
     *                       包含的消息类型：
     *                       <ul>
     *                       <li><b>SystemMessage</b>：设定 AI 角色和系统指令（通常放在列表开头）</li>
     *                       <li><b>UserMessage</b>：用户的历史输入</li>
     *                       <li><b>AssistantMessage</b>：AI 的历史回复（可能包含工具调用请求）</li>
     *                       <li><b>ToolResponseMessage</b>：工具执行结果（返回给 AI 继续推理）</li>
     *                       </ul>
     * @param userMetadata   用户元数据对象，包含 userId、sessionId 等信息。
     *                       可为 null，表示不传递用户上下文。
     *                       用于工具调用时识别用户身份和会话。
     * @param toolComponents 可变参数，工具回调数组，用于支持函数调用能力。
     *                       可为 null 或不传，表示不使用工具。
     *                       常见工具：时间查询、文件操作、API 调用等。
     * @return OwlChatResponse 包含 AI 的完整响应，可通过 response.content() 获取文本内容
     * @throws RuntimeException 如果 API 调用失败（网络错误、认证失败等）
     *
     *                          <h3>使用示例：</h3>
     *                          <pre>{@code
     * // 1. 简单对话（无用户元数据）
     * OwlChatResponse response = client.chat("你好", Collections.emptyList());
     * String answer = response.content();
     *
     * // 2. 带用户元数据的对话
     * UserMetadata metadata = UserMetadata.of("user-123", "session-456");
     * OwlChatResponse response = client.chat("你好", messages, metadata);
     *
     * // 3. 带工具的对话
     * OwlChatResponse response = client.chat("现在几点？", Collections.emptyList(), new TimeTools());
     *
     * // 4. 完整用法
     * UserMetadata metadata = UserMetadata.builder()
     *     .userId("user-123")
     *     .sessionId("session-456")
     *     .addMetadata("timezone", "Asia/Shanghai")
     *     .build();
     * OwlChatResponse response = client.chat("查询时间", messages, metadata, new TimeTools());
     * }</pre>
     * @see LLMAgentResponse
     * @see UserMetadata
     */
    public LLMAgentResponse chat(String userMessage, List<Message> messages, UserMetadata userMetadata, List<ToolComponent> toolComponents) {
        try {
            // 创建请求规范
            ChatClient.ChatClientRequestSpec spec = chatClient.prompt(userMessage);

            // 添加历史消息（如果存在）
            if (messages != null && !messages.isEmpty()) {
                spec.messages(messages);
            }

            // 添加工具回调（如果存在）
            if (toolComponents != null && !toolComponents.isEmpty()) {
                List<ToolCallback> callbackList = toolComponents.stream()
                        .map(ToolCallbacks::from)
                        .flatMap(Arrays::stream)
                        .toList();
                if (!CollectionUtils.isEmpty(callbackList)) {
                    spec.toolCallbacks(callbackList);
                }
            }

            // 设置用户元数据（如果存在）
            if (userMetadata != null) {
                spec.toolContext(userMetadata.toMap());
            } else {
                // 默认元数据
                HashMap<String, Object> defaultContext = new HashMap<>();
                defaultContext.put("userId", "default_user");
                spec.toolContext(defaultContext);
            }

            // 执行调用并获取 Spring AI 响应
            ChatClientResponse springResponse = spec.call().chatClientResponse();

            // 转换为 OwlChatResponse
            if (springResponse != null && springResponse.chatResponse() != null
                    && springResponse.chatResponse().getResult() != null
                    && springResponse.chatResponse().getResult().getOutput() != null) {
                String content = springResponse.chatResponse().getResult().getOutput().getText();
                return LLMAgentResponse.success(content);
            }

            // 如果响应为空，返回空内容
            return LLMAgentResponse.success("");

        } catch (Exception e) {
            // 记录错误并返回错误响应
            log.error("LLM 调用失败: {}", e.getMessage(), e);
            return LLMAgentResponse.error("LLM 调用失败: " + e.getMessage());
        }
    }

    /**
     * 流式调用 LLM，实时获取响应片段
     * <p>
     * 发送用户消息到 LLM，以流式方式逐块接收响应。适用于需要实时展示的场景，
     * 如聊天界面的打字机效果、长文本生成的进度显示等。
     * </p>
     *
     * <h3>特性：</h3>
     * <ul>
     *   <li>✅ 支持多轮对话上下文（通过 messages 参数）</li>
     *   <li>✅ 支持工具调用（通过 toolComponents 参数）</li>
     *   <li>✅ 非阻塞式响应式流，逐块发射响应片段</li>
     *   <li>✅ 低延迟，首字快速返回</li>
     *   <li>✅ 适合长文本生成和实时交互场景</li>
     * </ul>
     *
     * @param userMessage    用户当前输入消息，不能为 null 或空
     * @param messages       历史消息列表，用于维持对话上下文，可为 null 或空列表。
     *                       包含的消息类型：
     *                       <ul>
     *                       <li><b>SystemMessage</b>：设定 AI 角色和系统指令（通常放在列表开头）</li>
     *                       <li><b>UserMessage</b>：用户的历史输入</li>
     *                       <li><b>AssistantMessage</b>：AI 的历史回复（可能包含工具调用请求）</li>
     *                       <li><b>ToolResponseMessage</b>：工具执行结果（返回给 AI 继续推理）</li>
     *                       </ul>
     * @param toolComponents 可变参数，工具回调数组，用于支持函数调用能力。
     *                       可为 null 或不传，表示不使用工具。
     * @return Flux&lt;OwlChatResponse&gt; Reactor 响应式流，逐个发射 AI 响应片段。
     * 每个元素包含一部分响应文本，需要订阅才能触发实际调用。
     * <pre>{@code
     * flux.doOnNext(response -> {
     *     String chunk = response.content();
     *     System.out.print(chunk); // 实时打印
     * }).blockLast(); // 等待流完成
     * }</pre>
     * @throws RuntimeException 如果 API 调用失败（在订阅时抛出）
     *
     *                          <h3>使用示例：</h3>
     *                          <pre>{@code
     * // 基本流式调用
     * Flux<OwlChatResponse> stream = client.chatStream("写一首诗", Collections.emptyList());
     * StringBuilder fullText = new StringBuilder();
     * stream.doOnNext(response -> {
     *     String chunk = response.content();
     *     if (chunk != null) {
     *         fullText.append(chunk);
     *         System.out.print(chunk); // 打字机效果
     *     }
     * }).blockLast();
     *
     * // WebFlux 集成（无需 block）
     * return client.chatStream(message, history)
     *     .map(OwlChatResponse::content)
     *     .filter(text -> text != null);
     * }</pre>
     * @see Flux Reactor 响应式流
     * @see LLMAgentResponse
     */
    public Flux<LLMAgentResponse> chatStream(String userMessage, List<Message> messages, ToolComponent... toolComponents) {
        return chatStream(userMessage, messages, null, toolComponents);
    }

    /**
     * 流式调用 LLM（带用户元数据）
     *
     * @param userMessage    用户消息
     * @param messages       历史消息
     * @param userMetadata   用户元数据（可选）
     * @param toolComponents 工具组件
     * @return 响应式流
     */
    public Flux<LLMAgentResponse> chatStream(String userMessage, List<Message> messages, UserMetadata userMetadata, ToolComponent... toolComponents) {
        // 创建请求规范
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt(userMessage);

        // 添加历史消息（如果存在）
        if (messages != null && !messages.isEmpty()) {
            spec.messages(messages);
        }

        // 添加工具回调（如果存在）
        if (toolComponents != null && toolComponents.length > 0) {
            List<ToolCallback> callbackList = Arrays.stream(toolComponents)
                    .map(ToolCallbacks::from)
                    .flatMap(Arrays::stream)
                    .toList();
            if (!CollectionUtils.isEmpty(callbackList)) {
                spec.toolCallbacks(callbackList);
            }
        }

        // 设置用户元数据（如果存在）
        if (userMetadata != null) {
            spec.toolContext(userMetadata.toMap());
        } else {
            // 默认元数据
            HashMap<String, Object> defaultContext = new HashMap<>();
            defaultContext.put("userId", "default_user");
            spec.toolContext(defaultContext);
        }

        // 执行调用并将 Spring AI 响应转换为 OwlChatResponse
        return spec.stream()
                .chatClientResponse()
                .map(springResponse -> {
                    if (springResponse != null && springResponse.chatResponse() != null
                            && springResponse.chatResponse().getResult() != null
                            && springResponse.chatResponse().getResult().getOutput() != null) {
                        String content = springResponse.chatResponse().getResult().getOutput().getText();
                        return new LLMAgentResponse(content, null, null, null, null, true, null);
                    }
                    return new LLMAgentResponse(null, null, null, null, null, true, null);
                });
    }


    /**
     * 构建 RestClient，配置 HTTP 客户端和代理
     * <p>
     * 创建并配置用于 LLM API 调用的 RestClient，包括：
     * <ul>
     *   <li>HTTP/2 协议支持</li>
     *   <li>30 秒连接超时</li>
     *   <li>可选的 HTTP 代理配置</li>
     *   <li>JSON 请求/响应头</li>
     *   <li>请求日志拦截器（TRACE 级别）</li>
     * </ul>
     * </p>
     *
     * @param proxyDefinition 代理配置对象，可为 null。
     *                        如果不为 null 且 proxyEnabled=true，则启用代理。
     *                        包含：
     *                        <ul>
     *                        <li>proxyEnabled - 是否启用代理</li>
     *                        <li>host - 代理服务器主机地址</li>
     *                        <li>port - 代理服务器端口</li>
     *                        </ul>
     * @return 配置好的 RestClient.Builder 实例
     *
     * <h3>配置详情：</h3>
     * <ul>
     *   <li><b>HTTP 版本</b>：HTTP/2（提供更好的性能和多路复用）</li>
     *   <li><b>连接超时</b>：30 秒（防止网络问题导致长时间等待）</li>
     *   <li><b>Content-Type</b>：application/json</li>
     *   <li><b>Accept</b>：application/json</li>
     *   <li><b>日志级别</b>：TRACE（仅在启用 TRACE 日志时记录请求详情）</li>
     * </ul>
     *
     * <h3>代理配置示例：</h3>
     * <pre>{@code
     * LLMConfig.ProxyDefinition proxy = new LLMConfig.ProxyDefinition();
     * proxy.setProxyEnabled(true);
     * proxy.setHost("127.0.0.1");
     * proxy.setPort(7890);
     * config.setProxyDefinition(proxy);
     * }</pre>
     * @see LLMConfig.ProxyDefinition
     * @see java.net.http.HttpClient
     * @see JdkClientHttpRequestFactory
     */
    private RestClient.Builder restClientBuilder(LLMConfig.ProxyDefinition proxyDefinition) {
        // 创建 HTTP/2 客户端构建器
        java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))  // 连接超时30秒
                .version(java.net.http.HttpClient.Version.HTTP_2);  // 使用HTTP/2

        // 配置代理（如果启用）
        if (proxyDefinition != null && proxyDefinition.isProxyEnabled()) {
            log.debug("使用代理配置, host:{}, port:{}", proxyDefinition.getHost(), proxyDefinition.getPort());
            httpClientBuilder.proxy(ProxySelector.of(
                    InetSocketAddress.createUnresolved(proxyDefinition.getHost(), proxyDefinition.getPort())
            ));
        }

        // 构建 HTTP 客户端
        java.net.http.HttpClient httpClient = httpClientBuilder.build();

        // 创建请求工厂
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        // 构建 RestClient
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)  // 请求内容类型
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)        // 期望响应类型
                .requestInterceptor((request, body, execution) -> {
                    // 记录请求详情（TRACE 级别）
                    log.trace("HTTP请求拦截 - 请求URL:{}, 方法:{}, headers:{}",
                            request.getURI(), request.getMethod(), request.getHeaders());
                    return execution.execute(request, body);
                });
    }
}
