package com.owl.core.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.util.Collections;
import java.util.List;

/**
 * LLM 聊天请求对象
 * <p>
 * 封装 LLM 调用的所有参数，包括用户消息、历史消息、用户元数据等。
 * 工具组件通过 LLMClient 构造方法注入，不在此对象中。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 基本用法
 * ChatRequest request = ChatRequest.builder()
 *     .userMessage("你好")
 *     .build();
 *
 * // 2. 完整用法
 * ChatRequest request = ChatRequest.builder()
 *     .userMessage("查询时间")
 *     .messages(history)
 *     .userMetadata(metadata)
 *     .build();
 *
 * // 3. 便捷方法
 * ChatRequest request = ChatRequest.of("你好");
 * }</pre>
 *
 * @author OWL Team
 * @version 1.0
 * @see LLMClient
 * @since 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMChatRequest {

    /**
     * 用户当前输入消息
     * <p>
     * 必填字段，不能为 null 或空。
     * </p>
     */
    private String userMessage;

    /**
     * 历史消息列表
     * <p>
     * 用于维持对话上下文，可为 null 或空列表。
     * 包含的消息类型：
     * <ul>
     *   <li><b>SystemMessage</b>：设定 AI 角色和系统指令</li>
     *   <li><b>UserMessage</b>：用户的历史输入</li>
     *   <li><b>AssistantMessage</b>：AI 的历史回复</li>
     *   <li><b>ToolResponseMessage</b>：工具执行结果</li>
     * </ul>
     * </p>
     */
    @Builder.Default
    private List<Message> messages = Collections.emptyList();

    /**
     * 用户元数据
     * <p>
     * 包含 userId、sessionId 等信息，用于工具调用时识别用户身份。
     * 可为 null，表示不传递用户上下文。
     * </p>
     */
    private UserMetadata userMetadata;

    /**
     * 创建简单的聊天请求
     *
     * @param userMessage 用户消息
     * @return 聊天请求对象
     */
    public static LLMChatRequest of(String userMessage) {
        return LLMChatRequest.builder()
                .userMessage(userMessage)
                .build();
    }

    /**
     * 创建带历史消息的聊天请求
     *
     * @param userMessage 用户消息
     * @param messages    历史消息列表
     * @return 聊天请求对象
     */
    public static LLMChatRequest of(String userMessage, List<Message> messages) {
        return LLMChatRequest.builder()
                .userMessage(userMessage)
                .messages(messages != null ? messages : Collections.emptyList())
                .build();
    }

    /**
     * 创建完整的聊天请求
     *
     * @param userMessage  用户消息
     * @param messages     历史消息列表
     * @param userMetadata 用户元数据
     * @return 聊天请求对象
     */
    public static LLMChatRequest of(String userMessage, List<Message> messages, UserMetadata userMetadata) {
        return LLMChatRequest.builder()
                .userMessage(userMessage)
                .messages(messages != null ? messages : Collections.emptyList())
                .userMetadata(userMetadata)
                .build();
    }
}
