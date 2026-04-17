package com.owl.core.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 配置类
 * <p>
 * 封装大语言模型的连接配置和参数设置，用于创建 {@link LLMClient} 实例。
 * 支持多种 LLM 平台（火山引擎、MiniMax、百度千帆等），通过 {@link LLMPlatformEnum} 统一管理。
 * </p>
 *
 * <h3>配置项说明：</h3>
 * <ul>
 *   <li><b>llmPlatform</b> - LLM 平台枚举，决定 API 地址和接口路径</li>
 *   <li><b>apiKey</b> - API 密钥，用于身份认证</li>
 *   <li><b>model</b> - 模型名称，指定使用的具体模型版本</li>
 *   <li><b>proxyDefinition</b> - 代理配置，可选，用于网络受限环境</li>
 *   <li><b>temperature</b> - 温度参数，控制输出随机性（0-2）</li>
 *   <li><b>maxTokens</b> - 最大 Token 数，限制响应长度</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 创建配置对象
 * LLMConfig config = new LLMConfig();
 * config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
 * config.setApiKey("your-api-key");
 * config.setModel("doubao-seed-2.0-lite");
 * config.setTemperature(0.2);
 * config.setMaxTokens(2048);
 *
 * // 2. （可选）配置代理
 * LLMConfig.ProxyDefinition proxy = new LLMConfig.ProxyDefinition();
 * proxy.setProxyEnabled(true);
 * proxy.setHost("127.0.0.1");
 * proxy.setPort(7890);
 * config.setProxyDefinition(proxy);
 *
 * // 3. 创建客户端
 * LLMClient client = LLMClient.create(config);
 * }</pre>
 *
 * @author OWL Team
 * @version 1.0
 * @see LLMPlatformEnum
 * @see LLMClient
 * @since 2026-04-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMConfig {

    /**
     * LLM 平台枚举
     * <p>
     * 指定使用的大模型平台，决定 API 的基础 URL 和对话接口路径。
     * 支持的 platform包括：火山引擎、MiniMax、百度千帆、阿里云通义等。
     * </p>
     *
     * @see LLMPlatformEnum
     */
    private LLMPlatformEnum llmPlatform;

    /**
     * LLM API 密钥
     * <p>
     * 用于身份认证的 API Key，从 LLM 平台控制台获取。
     * 必填字段，不能为 null 或空字符串。
     * </p>
     *
     * <h3>获取方式：</h3>
     * <ul>
     *   <li>火山引擎：https://console.volcengine.com/ark</li>
     *   <li>MiniMax：https://platform.minimaxi.com</li>
     *   <li>百度千帆：https://console.bce.baidu.com/qianfan</li>
     * </ul>
     *
     * <h3>安全建议：</h3>
     * <ul>
     *   <li>不要将 API Key 硬编码在代码中</li>
     *   <li>使用环境变量或配置中心管理</li>
     *   <li>定期轮换密钥</li>
     * </ul>
     */
    private String apiKey;

    /**
     * 使用的模型名称
     * <p>
     * 指定要调用的具体模型版本。不同平台有不同的模型命名规则。
     * </p>
     *
     * <h3>常见模型示例：</h3>
     * <ul>
     *   <li>火山引擎：doubao-seed-2.0-lite, doubao-seed-2.0-pro</li>
     *   <li>MiniMax：MiniMax-M2.7</li>
     *   <li>百度文心：ernie-bot-4</li>
     *   <li>阿里通义：qwen-max</li>
     *   <li>Kimi：moonshot-v1-8k</li>
     *   <li>DeepSeek：deepseek-chat</li>
     * </ul>
     *
     * @see LLMPlatformEnum 各平台的默认模型
     */
    private String model;

    /**
     * 代理配置
     * <p>
     * 可选的 HTTP 代理设置，用于网络受限环境（如企业内网）。
     * 如果为 null 或 proxyEnabled=false，则直连 LLM API。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>企业内网需要代理访问外网</li>
     *   <li>需要通过特定出口 IP 访问 API</li>
     *   <li>网络调试和监控</li>
     * </ul>
     *
     * @see ProxyDefinition
     */
    private ProxyDefinition proxyDefinition;

    /**
     * 温度参数 - 控制输出的随机性和创造性
     * <p>
     * 影响 AI 生成文本的多样性和可预测性。
     * </p>
     *
     * <h3>取值范围：</h3>
     * <ul>
     *   <li><b>0.0 - 0.3</b>：严谨、固定、可预测（适合代码生成、事实问答）</li>
     *   <li><b>0.4 - 0.7</b>：平衡、自然（适合日常对话、通用任务）</li>
     *   <li><b>0.8 - 1.5</b>：创意、多样（适合创意写作、头脑风暴）</li>
     *   <li><b>1.6 - 2.0</b>：高度随机、实验性（慎用）</li>
     * </ul>
     *
     * <h3>推荐配置：</h3>
     * <ul>
     *   <li>企业 Agent：0.1 ~ 0.3（保证稳定性和准确性）</li>
     *   <li>客服机器人：0.3 ~ 0.5（平衡友好性和准确性）</li>
     *   <li>创意助手：0.7 ~ 1.0（激发创造力）</li>
     * </ul>
     *
     * <h3>默认值：</h3>
     * <ul>
     *   <li>当前默认：0.5</li>
     *   <li>null 时：使用平台默认值（通常为 0.5-0.7）</li>
     * </ul>
     */
    private Double temperature = 0.5;

    /**
     * 最大 Token 数
     * <p>
     * 限制 AI 响应的最大长度（包括输入和输出的总 Token 数）。
     * Token 是文本的基本单位，1 个中文汉字 ≈ 1-2 个 Token，1 个英文单词 ≈ 1-1.5 个 Token。
     * </p>
     *
     * <h3>取值建议：</h3>
     * <ul>
     *   <li><b>512 - 1024</b>：短回复（简单问答、摘要）</li>
     *   <li><b>1024 - 2048</b>：中等长度（详细解释、代码片段）</li>
     *   <li><b>2048 - 4096</b>：长文本（文章生成、复杂分析）</li>
     *   <li><b>4096+</b>：超长文本（文档处理、长篇创作）</li>
     * </ul>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>不同模型有不同的最大 Token 限制（如 4K、8K、32K、128K）</li>
     *   <li>设置的值不能超过模型的最大限制</li>
     *   <li>Token 数越多，响应时间越长，费用越高</li>
     *   <li>null 时使用平台默认值（通常为 1024-2048）</li>
     * </ul>
     *
     * @see <a href="https://platform.openai.com/tokenizer">OpenAI Tokenizer</a>
     */
    private Integer maxTokens;

    /**
     * 代理配置内部类
     * <p>
     * 封装 HTTP 代理服务器的配置信息，用于在网络受限环境下访问 LLM API。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * ProxyDefinition proxy = new ProxyDefinition();
     * proxy.setProxyEnabled(true);
     * proxy.setHost("127.0.0.1");
     * proxy.setPort(7890);
     * config.setProxyDefinition(proxy);
     * }</pre>
     *
     * <h3>常见代理工具：</h3>
     * <ul>
     *   <li>Clash：默认端口 7890</li>
     *   <li>V2Ray：默认端口 1080</li>
     *   <li>Squid：默认端口 3128</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProxyDefinition {
        /**
         * 是否启用代理
         * <p>
         * true - 启用代理，使用 host 和 port 配置
         * false - 禁用代理，直连 LLM API
         * </p>
         *
         * @default false
         */
        private boolean proxyEnabled = false;

        /**
         * 代理服务器主机地址
         * <p>
         * 可以是 IP 地址或域名。
         * </p>
         *
         * <h3>示例：</h3>
         * <ul>
         *   <li>IP 地址："127.0.0.1"</li>
         *   <li>域名："proxy.example.com"</li>
         *   <li>IPv6："::1"</li>
         * </ul>
         */
        private String host;

        /**
         * 代理服务器端口号
         * <p>
         * 有效的端口范围：1 - 65535
         * </p>
         *
         * <h3>常见端口：</h3>
         * <ul>
         *   <li>HTTP 代理：8080, 3128</li>
         *   <li>SOCKS5 代理：1080</li>
         *   <li>Clash：7890</li>
         * </ul>
         */
        private int port;
    }
}
