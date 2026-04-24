package com.owl.core.llm;

/**
 * LLM 客户端异常
 * <p>
 * 当 LLMClient 初始化或调用过程中发生错误时抛出。
 * </p>
 *
 * <h3>常见场景：</h3>
 * <ul>
 *   <li>LLMClient 初始化失败（如 API Key 无效）</li>
 *   <li>网络连接失败（连接超时、DNS 解析失败）</li>
 *   <li>API 调用失败（401 未授权、429 请求限流、500 服务器错误）</li>
 *   <li>响应解析失败</li>
 * </ul>
 *
 * @author Owl Team
 * @see LLMClient
 * @since 2026-04-23
 */
public class LLMClientException extends RuntimeException {

    public LLMClientException(String message) {
        super(message);
    }

    public LLMClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
