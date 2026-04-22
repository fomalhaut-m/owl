package com.owl.core.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户元数据对象
 * <p>
 * 封装用户的上下文信息，用于传递给 LLM 工具调用。
 * 包含用户 ID、会话 ID 以及其他自定义元数据。
 * </p>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>工具调用时传递用户身份信息</li>
 *   <li>个性化推荐和响应</li>
 *   <li>权限控制和审计日志</li>
 *   <li>多租户隔离</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 基本用法
 * UserMetadata metadata = UserMetadata.builder()
 *     .userId("user-123")
 *     .sessionId("session-456")
 *     .build();
 *
 * // 2. 添加自定义元数据
 * metadata.addMetadata("timezone", "Asia/Shanghai");
 * metadata.addMetadata("language", "zh-CN");
 *
 * // 3. 在 LLM 调用中使用
 * OwlChatResponse response = client.chat(
 *     "你好",
 *     messages,
 *     tools,
 *     metadata
 * );
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
public class UserMetadata {

    /**
     * 用户唯一标识
     * <p>
     * 用于识别当前操作用户，是必须的字段。
     * </p>
     */
    private String userId;

    /**
     * 会话 ID
     * <p>
     * 标识当前对话会话，用于追踪和上下文管理。
     * 可选字段，如果不提供则由系统自动生成。
     * </p>
     */
    private String sessionId;

    /**
     * 自定义元数据
     * <p>
     * 存储额外的用户相关信息，如：
     * - 时区（timezone）
     * - 语言偏好（language）
     * - 用户角色（role）
     * - 其他业务相关数据
     * </p>
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 添加自定义元数据
     *
     * @param key   元数据键
     * @param value 元数据值
     * @return 当前对象，支持链式调用
     */
    public UserMetadata addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }

    /**
     * 获取自定义元数据
     *
     * @param key 元数据键
     * @return 元数据值，如果不存在返回 null
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * 转换为 Map，用于传递给 LLM 工具上下文
     *
     * @return 包含所有元数据的 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        if (sessionId != null) {
            map.put("sessionId", sessionId);
        }
        if (metadata != null && !metadata.isEmpty()) {
            map.putAll(metadata);
        }
        return map;
    }

    /**
     * 创建默认的用户元数据
     *
     * @param userId 用户 ID
     * @return 默认的用户元数据对象
     */
    public static UserMetadata of(String userId) {
        return UserMetadata.builder()
                .userId(userId)
                .build();
    }

    /**
     * 创建完整的用户元数据
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 完整的用户元数据对象
     */
    public static UserMetadata of(String userId, String sessionId) {
        return UserMetadata.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
    }
}
