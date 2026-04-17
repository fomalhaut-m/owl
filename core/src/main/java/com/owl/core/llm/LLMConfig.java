package com.owl.core.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 配置类
 * 包含大语言模型的连接配置和参数设置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMConfig {

    /// LLM 平台
    private LLMPlatformEnum llmPlatform;

    /// LLM API 密钥
    private String apiKey;

    /// 使用的模型名称
    private String model;

    /// 代理配置
    private ProxyDefinition proxyDefinition;

    /// 温度参数 - 控制输出的随机性
    /// 范围：0 ~ 2
    /// 越低越严谨、固定（企业 Agent 推荐 0.1 ~ 0.3）
    /// 越高越随机、创意
    /// null 时使用默认值 0.5
    private Double temperature = 0.5;

    /// 最大 Token 数
    /// 默认值由 LLM 提供商决定，通常建议设置为 1024 或更高
    private Integer maxTokens;

    /**
     * 代理配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProxyDefinition {
        private boolean proxyEnabled = false;
        private String host;
        private int port;
    }
}
