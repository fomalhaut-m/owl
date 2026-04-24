package com.owl.core.llm;

import com.owl.core.skills.tools.ToolComponent;
import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.*;

/**
 * LLM 客户端（Builder 模式）
 * <p>
 * 通过链式调用构建请求并执行 LLM 调用。
 * 支持同步和流式两种调用方式，兼容多种 LLM 平台。
 * </p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>✅ Builder 模式 - 链式调用，API 简洁直观</li>
 *   <li>✅ 同步调用 - {#call()} 返回完整响应</li>
 *   <li>✅ 流式调用 - {#callStream()} 实时获取响应片段</li>
 *   <li>✅ 工具调用 - 支持 ToolComponent 扩展 AI 能力</li>
 *   <li>✅ 多轮对话 - 支持 SystemMessage、AssistantMessage、ToolResponseMessage</li>
 *   <li>✅ 运行时参数 - model、temperature、maxTokens 可动态设置</li>
 *   <li>✅ 工具名称校验 - 自动检测 @Tool 名称重复</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 简单对话
 * LLMAgentResponse response = LLMClient.create(config)
 *     .chat("你好")
 *     .call();
 * System.out.println(response.content());
 *
 * // 2. 带系统提示的多轮对话
 * LLMAgentResponse response = LLMClient.create(config)
 *     .chat("解释什么是 SOLID 原则")
 *     .systemMessage("你是一个技术专家，用简洁的语言解释概念")
 *     .call();
 *
 * // 3. 带工具调用的对话
 * LLMAgentResponse response = LLMClient.create(config)
 *     .chat("现在几点？")
 *     .tool(new TimeTools())
 *     .call();
 *
 * // 4. 带历史消息的对话
 * LLMAgentResponse response = LLMClient.create(config)
 *     .chat("继续")
 *     .systemMessage("你是一个诗人")
 *     .assistantMessage("春风又绿江南岸")
 *     .call();
 *
 * // 5. 运行时覆盖模型参数
 * LLMAgentResponse response = LLMClient.create(config)
 *     .chat("写一首诗")
 *     .model("doubao-seed-2.0-pro")
 *     .temperature(0.8)
 *     .maxTokens(500)
 *     .call();
 *
 * // 6. 流式调用
 * LLMClient.create(config)
 *     .chat("写一个故事")
 *     .callStream()
 *     .doOnNext(r -> System.out.print(r.content()))
 *     .blockLast();
 * }</pre>
 *
 * <h3>异常处理：</h3>
 * <ul>
 *   <li>{@link LLMClientException} - 客户端异常基类</li>
 *   <li>{@link IllegalArgumentException} - 参数校验失败（配置为空、userMessage 为空等）</li>
 *   <li>{@link HttpClientErrorException} - 客户端错误（401 未授权、429 请求过多等）</li>
 *   <li>{@link HttpServerErrorException} - 服务器错误（500 内部错误等）</li>
 *   <li>{@link ResourceAccessException} - 网络错误（连接超时、DNS 解析失败等）</li>
 * </ul>
 *
 * @author Owl Team
 * @see LLMAgentResponse
 * @see LLMPlatform
 * @since 2026-04-23
 */
@Slf4j
public class LLMClient {

    /** Spring AI ChatClient 实例 */
    private final ChatClient chatClient;

    /** 默认模型名称 */
    private final String defaultModel;

    /** 默认温度参数 */
    private final Double defaultTemperature;

    /** Validator 实例 */
    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    /**
     * 私有构造函数，通过 {@link #create(LLMConfig)} 创建实例
     *
     * @param llmConfig LLM 配置
     * @throws IllegalArgumentException 配置为空或缺少必填字段
     * @throws LLMClientException 初始化失败
     */
    private LLMClient(LLMConfig llmConfig) {
        // 参数校验
        if (llmConfig == null) {
            throw new IllegalArgumentException("LLMConfig 不能为空");
        }

        // 使用 @Valid 注解进行校验
        Set<ConstraintViolation<LLMConfig>> violations = VALIDATOR.validate(llmConfig);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("配置校验失败");
            throw new IllegalArgumentException(errorMsg);
        }

        // 校验代理配置
        if (llmConfig.getProxyDefinition() != null && llmConfig.getProxyDefinition().isProxyEnabled()) {
            LLMConfig.ProxyDefinition proxy = llmConfig.getProxyDefinition();
            Set<ConstraintViolation<LLMConfig.ProxyDefinition>> proxyViolations = VALIDATOR.validate(proxy, LLMConfig.WhenProxyEnabled.class);
            if (!proxyViolations.isEmpty()) {
                String errorMsg = proxyViolations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("代理配置校验失败");
                throw new IllegalArgumentException(errorMsg);
            }
        }

        try {
            ChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .apiKey(llmConfig.getApiKey())
                            .baseUrl(llmConfig.getLlmPlatform().getBaseUrl())
                            .completionsPath(llmConfig.getLlmPlatform().getChatPath())
                            .restClientBuilder(restClientBuilder(llmConfig.getProxyDefinition()))
                            .build())
                    .build();

            this.chatClient = ChatClient.builder(chatModel).build();
            this.defaultModel = llmConfig.getModel();
            this.defaultTemperature = llmConfig.getTemperature();
            log.debug("LLMClient 初始化成功，平台: {}，默认模型: {}", llmConfig.getLlmPlatform().getPlatformName(), this.defaultModel);
        } catch (Exception e) {
            throw new LLMClientException("LLMClient 初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建 LLMClient 实例
     *
     * @param llmConfig LLM 配置
     * @return LLMClient 实例
     * @throws IllegalArgumentException 配置为空或缺少必填字段
     */
    public static LLMClient create(LLMConfig llmConfig) {
        return new LLMClient(llmConfig);
    }

    /**
     * 开始构建聊天请求
     *
     * @param userMessage 用户消息，必填
     * @return ChatRequestBuilder 用于链式配置
     * @throws IllegalArgumentException userMessage 为空
     */
    public ChatRequestBuilder chat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("userMessage 不能为空");
        }
        return new ChatRequestBuilder(userMessage, defaultModel, defaultTemperature);
    }

    /**
     * 聊天请求构建器
     * <p>
     * 通过链式调用配置请求参数，最后调用 {@link #call()} 或 {@link #callStream()} 执行。
     * </p>
     */
    public class ChatRequestBuilder {
        private final String userMessage;
        private final List<Message> messages = new ArrayList<>();
        private final List<ToolComponent> tools = new ArrayList<>();
        private final Set<String> registeredToolNames = new HashSet<>();
        private UserMetadata userMetadata;
        private String model;
        private Double temperature;
        private Integer maxTokens;

        ChatRequestBuilder(String userMessage, String defaultModel, Double defaultTemperature) {
            this.userMessage = userMessage;
            this.model = defaultModel;
            this.temperature = defaultTemperature;
        }

        /**
         * 添加系统消息
         * <p>
         * 系统消息用于设定 AI 的角色、行为规则或背景信息。
         * 通常放在消息列表的开头，对整个对话生效。
         * </p>
         *
         * @param content 系统提示内容
         * @return this
         */
        public ChatRequestBuilder systemMessage(String content) {
            if (content != null && !content.isBlank()) {
                messages.add(new SystemMessage(content));
            }
            return this;
        }

        /**
         * 添加助手消息（历史）
         * <p>
         * 用于传入对话历史中 AI 的回复，帮助维持上下文。
         * </p>
         *
         * @param content 助手回复内容
         * @return this
         */
        public ChatRequestBuilder assistantMessage(String content) {
            if (content != null && !content.isBlank()) {
                messages.add(new AssistantMessage(content));
            }
            return this;
        }

        /**
         * 添加工具响应消息
         * <p>
         * 在多轮工具调用中，将工具执行结果返回给 AI。
         * </p>
         *
         * @param toolCallId 工具调用 ID（来自 AI 的 tool_calls）
         * @param content 工具执行结果
         * @return this
         */
        public ChatRequestBuilder toolResponse(String toolCallId, String content) {
            if (toolCallId != null && content != null) {
                messages.add(new ToolResponseMessage(
                        List.of(new ToolResponseMessage.ToolResponse(toolCallId, "tool", content))));
            }
            return this;
        }

        /**
         * 添加工具组件（带唯一性校验）
         * <p>
         * 校验规则：
         * <ul>
         *   <li>Tool 名称不能重复，否则抛出 {@link LLMClientException}</li>
         * </ul>
         * </p>
         *
         * @param tool 工具组件
         * @return this
         * @throws LLMClientException 工具名称重复
         */
        public ChatRequestBuilder tool(ToolComponent tool) {
            if (tool != null) {
                validateToolNamesUnique(tool);
                tools.add(tool);
            }
            return this;
        }

        /**
         * 批量添加工具组件（带唯一性校验）
         * <p>
         * 校验规则：
         * <ul>
         *   <li>Tool 名称不能重复，否则抛出 {@link LLMClientException}</li>
         * </ul>
         * </p>
         *
         * @param toolComponents 工具组件数组
         * @return this
         * @throws LLMClientException 工具名称重复
         */
        public ChatRequestBuilder tools(ToolComponent... toolComponents) {
            if (toolComponents != null) {
                for (ToolComponent tool : toolComponents) {
                    if (tool != null) {
                        validateToolNamesUnique(tool);
                        this.tools.add(tool);
                    }
                }
            }
            return this;
        }

        /**
         * 校验工具组件的 @Tool 名称是否与已注册名称重复
         *
         * @param toolComponent 工具组件
         * @throws LLMClientException 如果名称重复
         */
        private void validateToolNamesUnique(ToolComponent toolComponent) {
            Class<?> clazz = toolComponent.getClass();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                if (toolAnnotation != null) {
                    String toolName = toolAnnotation.name();
                    if (toolName != null && !toolName.isBlank()) {
                        if (!registeredToolNames.add(toolName)) {
                            throw new LLMClientException(
                                    String.format("Tool 名称 '%s' 重复，已在之前的工具中注册", toolName));
                        }
                    }
                }
            }
        }

        /**
         * 设置用户元数据
         * <p>
         * 用于传递 userId、sessionId 等上下文信息给工具。
         * </p>
         *
         * @param userMetadata 用户元数据
         * @return this
         * @see UserMetadata
         */
        public ChatRequestBuilder userMetadata(UserMetadata userMetadata) {
            this.userMetadata = userMetadata;
            return this;
        }

        /**
         * 设置模型名称
         * <p>
         * 覆盖 LLMConfig 中的默认模型。
         * </p>
         *
         * @param model 模型名称，如 "doubao-seed-2.0-pro"
         * @return this
         */
        public ChatRequestBuilder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * 设置温度参数
         * <p>
         * 控制输出的随机性：
         * <ul>
         *   <li>0.0-0.3：确定性输出，适合代码、精确回答</li>
         *   <li>0.4-0.7：平衡模式</li>
         *   <li>0.8-1.0：创意输出，适合故事、诗歌</li>
         * </ul>
         * </p>
         *
         * @param temperature 温度值，建议范围 0.0-2.0
         * @return this
         */
        public ChatRequestBuilder temperature(Double temperature) {
            if (temperature != null && (temperature < 0 || temperature > 2.0)) {
                throw new IllegalArgumentException("temperature 建议范围为 0.0-2.0，当前值: " + temperature);
            }
            this.temperature = temperature;
            return this;
        }

        /**
         * 设置最大 Token 数
         * <p>
         * 限制单次响应的最大长度。
         * </p>
         *
         * @param maxTokens 最大 Token 数
         * @return this
         */
        public ChatRequestBuilder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException("maxTokens 必须大于 0，当前值: " + maxTokens);
            }
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * 同步调用 LLM
         * <p>
         * 发送请求并等待完整响应返回。
         * 适用于需要等待完整结果的场景。
         * </p>
         *
         * @return LLMAgentResponse 响应结果
         * @throws LLMClientException 调用失败
         */
        public LLMAgentResponse call() {
            try {
                // 构建请求规范
                ChatClient.ChatClientRequestSpec spec = chatClient.prompt(userMessage);

                // 添加历史消息
                if (!messages.isEmpty()) {
                    spec.messages(messages);
                }

                // 添加工具
                if (!tools.isEmpty()) {
                    List<ToolCallback> callbackList = tools.stream()
                            .map(ToolCallbacks::from)
                            .flatMap(Arrays::stream)
                            .toList();
                    if (!CollectionUtils.isEmpty(callbackList)) {
                        spec.toolCallbacks(callbackList);
                        log.debug("注册 {} 个工具回调", callbackList.size());
                    }
                }

                // 设置用户元数据
                if (userMetadata != null) {
                    spec.toolContext(userMetadata.toMap());
                } else {
                    HashMap<String, Object> defaultContext = new HashMap<>();
                    defaultContext.put("userId", "default_user");
                    spec.toolContext(defaultContext);
                }

                // 设置模型参数
                if (model != null || temperature != null || maxTokens != null) {
                    OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
                    if (model != null) optionsBuilder.model(model);
                    if (temperature != null) optionsBuilder.temperature(temperature);
                    if (maxTokens != null) optionsBuilder.maxTokens(maxTokens);
                    spec.options(optionsBuilder.build());
                }

                // 执行调用
                ChatClientResponse springResponse = spec.call().chatClientResponse();

                // 解析响应
                if (springResponse != null && springResponse.chatResponse() != null
                        && springResponse.chatResponse().getResult() != null
                        && springResponse.chatResponse().getResult().getOutput() != null) {
                    String content = springResponse.chatResponse().getResult().getOutput().getText();
                    log.debug("LLM 同步调用成功，响应长度: {}", content != null ? content.length() : 0);
                    return LLMAgentResponse.success(content);
                }

                return LLMAgentResponse.success("");

            } catch (HttpClientErrorException e) {
                // 4xx 客户端错误
                String errorMsg = String.format("LLM API 客户端错误 [%d]: %s", e.getStatusCode().value(), e.getResponseBodyAsString());
                log.error(errorMsg);
                throw new LLMClientException(errorMsg, e);
            } catch (HttpServerErrorException e) {
                // 5xx 服务器错误
                String errorMsg = String.format("LLM API 服务器错误 [%d]: %s", e.getStatusCode().value(), e.getResponseBodyAsString());
                log.error(errorMsg);
                throw new LLMClientException(errorMsg, e);
            } catch (ResourceAccessException e) {
                // 网络错误
                String errorMsg = "LLM API 网络连接失败: " + e.getMessage();
                log.error(errorMsg);
                throw new LLMClientException(errorMsg, e);
            } catch (Exception e) {
                log.error("LLM 调用未预期错误: {}", e.getMessage(), e);
                throw new LLMClientException("LLM 调用失败: " + e.getMessage(), e);
            }
        }

        /**
         * 流式调用 LLM
         * <p>
         * 以流式方式实时获取响应片段，适用于聊天界面打字机效果。
         * </p>
         *
         * @return Flux&lt;LLMAgentResponse&gt; 响应流
         * @throws LLMClientException 调用失败
         */
        public Flux<LLMAgentResponse> callStream() {
            try {
                // 构建请求规范
                ChatClient.ChatClientRequestSpec spec = chatClient.prompt(userMessage);

                // 添加历史消息
                if (!messages.isEmpty()) {
                    spec.messages(messages);
                }

                // 添加工具
                if (!tools.isEmpty()) {
                    List<ToolCallback> callbackList = tools.stream()
                            .map(ToolCallbacks::from)
                            .flatMap(Arrays::stream)
                            .toList();
                    if (!CollectionUtils.isEmpty(callbackList)) {
                        spec.toolCallbacks(callbackList);
                        log.debug("注册 {} 个工具回调（流式）", callbackList.size());
                    }
                }

                // 设置用户元数据
                if (userMetadata != null) {
                    spec.toolContext(userMetadata.toMap());
                } else {
                    HashMap<String, Object> defaultContext = new HashMap<>();
                    defaultContext.put("userId", "default_user");
                    spec.toolContext(defaultContext);
                }

                // 设置模型参数
                if (model != null || temperature != null || maxTokens != null) {
                    OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
                    if (model != null) optionsBuilder.model(model);
                    if (temperature != null) optionsBuilder.temperature(temperature);
                    if (maxTokens != null) optionsBuilder.maxTokens(maxTokens);
                    spec.options(optionsBuilder.build());
                }

                // 执行流式调用
                log.debug("开始 LLM 流式调用");
                return spec.stream()
                        .chatClientResponse()
                        .map(response -> {
                            if (response != null && response.chatResponse() != null
                                    && response.chatResponse().getResult() != null
                                    && response.chatResponse().getResult().getOutput() != null) {
                                String content = response.chatResponse().getResult().getOutput().getText();
                                return LLMAgentResponse.success(content);
                            }
                            return LLMAgentResponse.success("");
                        })
                        .doOnError(e -> log.error("LLM 流式调用异常: {}", e.getMessage(), e))
                        .doOnComplete(() -> log.debug("LLM 流式调用完成"));

            } catch (HttpClientErrorException e) {
                String errorMsg = String.format("LLM API 客户端错误 [%d]: %s", e.getStatusCode().value(), e.getResponseBodyAsString());
                log.error(errorMsg);
                return Flux.error(new LLMClientException(errorMsg, e));
            } catch (HttpServerErrorException e) {
                String errorMsg = String.format("LLM API 服务器错误 [%d]: %s", e.getStatusCode().value(), e.getResponseBodyAsString());
                log.error(errorMsg);
                return Flux.error(new LLMClientException(errorMsg, e));
            } catch (ResourceAccessException e) {
                String errorMsg = "LLM API 网络连接失败: " + e.getMessage();
                log.error(errorMsg);
                return Flux.error(new LLMClientException(errorMsg, e));
            } catch (Exception e) {
                log.error("LLM 流式调用失败: {}", e.getMessage(), e);
                return Flux.error(new LLMClientException("LLM 流式调用失败: " + e.getMessage(), e));
            }
        }
    }

    /**
     * 构建 RestClient，配置 HTTP 客户端和代理
     */
    private RestClient.Builder restClientBuilder(LLMConfig.ProxyDefinition proxyDefinition) {
        java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .version(java.net.http.HttpClient.Version.HTTP_2);

        if (proxyDefinition != null && proxyDefinition.isProxyEnabled()) {
            log.debug("使用代理配置, host:{}, port:{}", proxyDefinition.getHost(), proxyDefinition.getPort());
            httpClientBuilder.proxy(ProxySelector.of(
                    InetSocketAddress.createUnresolved(proxyDefinition.getHost(), proxyDefinition.getPort())
            ));
        }

        java.net.http.HttpClient httpClient = httpClientBuilder.build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    log.trace("HTTP请求 - URL:{}, 方法:{}", request.getURI(), request.getMethod());
                    return execution.execute(request, body);
                });
    }
}
