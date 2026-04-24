package com.owl.core.llm;

/**
 * LLM 平台配置接口
 * <p>
 * 定义 LLM 平台所需的基础配置信息，方便扩展和自定义。
 * </p>
 *
 * <h3>扩展方式：</h3>
 * <pre>{@code
 * // 1. 通过枚举实现
 * public enum CustomPlatform implements LLMPlatform {
 *     CUSTOM("自定义", "https://api.example.com", "/v1/chat");
 *     
 *     private final String platformName;
 *     private final String baseUrl;
 *     private final String chatPath;
 *     
 *     CustomPlatform(String platformName, String baseUrl, String chatPath) {
 *         this.platformName = platformName;
 *         this.baseUrl = baseUrl;
 *         this.chatPath = chatPath;
 *     }
 * }
 * 
 * // 2. 通过类实现
 * public class CustomLLMPlatform implements LLMPlatform {
 *     // 自定义实现
 * }
 * }</pre>
 *
 * @author OWL Team
 * @version 1.0
 * @since 2026-04-23
 */
public interface LLMPlatform {

    /**
     * 获取平台显示名称
     *
     * @return 平台中文名称
     */
    String getPlatformName();

    /**
     * 获取 API 基础地址
     *
     * @return API 服务器地址
     */
    String getBaseUrl();

    /**
     * 获取对话接口路径
     *
     * @return 相对路径
     */
    String getChatPath();
}