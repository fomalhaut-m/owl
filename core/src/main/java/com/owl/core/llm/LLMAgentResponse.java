package com.owl.core.llm;

/**
 * OWL 聊天响应对象（Record）
 * <p>
 * 简化的聊天响应封装类，用于传递 LLM 的回复内容。
 * 使用 Java Record 实现不可变的数据载体，不依赖 Spring AI 的 ChatClientResponse。
 * </p>
 *
 * <h3>主要特点：</h3>
 * <ul>
 *   <li>✅ 不可变 - Record 天然不可变，线程安全</li>
 *   <li>✅ 轻量级 - 只包含必要的字段</li>
 *   <li>✅ 简单 - 易于理解和使用</li>
 *   <li>✅ 独立 - 不依赖外部框架的复杂对象</li>
 *   <li>✅ 简洁 - 自动生成 getter、equals、hashCode、toString</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 同步调用
 * OwlChatResponse response = client.chat("你好", messages, tools);
 * System.out.println(response.content());
 *
 * // 2. 流式调用
 * Flux<OwlChatResponse> stream = client.chatStream("写一首诗", messages, tools);
 * stream.doOnNext(r -> System.out.print(r.content()))
 *       .blockLast();
 *
 * // 3. 创建响应
 * OwlChatResponse success = OwlChatResponse.success("回复内容");
 * OwlChatResponse error = OwlChatResponse.error("错误信息");
 * }</pre>
 *
 * @param content      AI 回复的文本内容
 * @param sessionId    会话 ID（可选），用于追踪和日志记录
 * @param userId       用户 ID（可选），用于个性化和权限控制
 * @param model        模型名称（可选），如 "doubao-seed-2.0-lite"
 * @param tokenUsage   Token 使用量统计（可选）
 * @param finished     是否完成（用于流式调用），true 表示响应已完成
 * @param errorMessage 错误信息（可选），成功时为 null
 *
 * @author OWL Team
 * @version 1.0
 * @see LLMClient
 * @since 2026-04-16
 */
public record LLMAgentResponse(
        String content,
        String sessionId,
        String userId,
        String model,
        TokenUsage tokenUsage,
        Boolean finished,
        String errorMessage
) {

    /**
     * 紧凑构造函数，提供默认值
     */
    public LLMAgentResponse {
        if (finished == null) {
            finished = true;
        }
    }

    /**
     * Token 使用量 Record
     * <p>
     * 封装 LLM 调用的 Token 消耗统计信息。
     * </p>
     *
     * @param promptTokens     输入 Token 数（Prompt）
     * @param completionTokens 输出 Token 数（Completion）
     * @param totalTokens      总 Token 数
     */
    public record TokenUsage(
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {
    }

    /**
     * 创建成功的响应
     * <p>
     * 便捷方法，快速创建一个成功的响应对象。
     * </p>
     *
     * @param content 回复内容
     * @return 成功的响应对象
     */
    public static LLMAgentResponse success(String content) {
        return new LLMAgentResponse(content, null, null, null, null, true, null);
    }

    /**
     * 创建失败的响应
     * <p>
     * 便捷方法，快速创建一个失败的响应对象。
     * </p>
     *
     * @param errorMessage 错误信息
     * @return 失败的响应对象
     */
    public static LLMAgentResponse error(String errorMessage) {
        return new LLMAgentResponse(null, null, null, null, null, true, errorMessage);
    }

    /**
     * 判断响应是否成功
     *
     * @return true 表示成功，false 表示失败
     */
    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    /**
     * 获取内容，如果为空则返回默认值
     *
     * @param defaultValue 默认值
     * @return 内容或默认值
     */
    public String getContentOrDefault(String defaultValue) {
        return content != null ? content : defaultValue;
    }
}
