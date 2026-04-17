package com.owl.core.llm;

import lombok.Getter;

/**
 * 国内主流 AI 大模型平台枚举
 * <p>
 * 封装各大 LLM 平台的 API 配置信息，包括平台名称、基础 URL、对话接口路径。
 * 通过统一管理不同平台的接入方式，简化 LLMClient 的配置流程。
 * </p>
 *
 * <h3>支持的平台：</h3>
 * <table border="1">
 *   <tr><th>平台</th><th>提供商</th><th>特色</th></tr>
 *   <tr><td>火山方舟</td><td>字节跳动</td><td>豆包系列，代码能力强</td></tr>
 *   <tr><td>百度千帆</td><td>百度</td><td>文心一言，中文理解优秀</td></tr>
 *   <tr><td>阿里云通义</td><td>阿里巴巴</td><td>通义千问，多语言支持</td></tr>
 *   <tr><td>腾讯混元</td><td>腾讯</td><td>混元大模型，生态整合</td></tr>
 *   <tr><td>智谱 GLM</td><td>智谱 AI</td><td>GLM 系列，开源友好</td></tr>
 *   <tr><td>Kimi</td><td>月之暗面</td><td>长文本处理能力强</td></tr>
 *   <tr><td>DeepSeek</td><td>深度求索</td><td>性价比高，性能优秀</td></tr>
 *   <tr><td>MiniMax</td><td>MiniMax</td><td>对话体验好，多模态</td></tr>
 * </table>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 获取平台配置
 * LLMPlatformEnum platform = LLMPlatformEnum.HUOSHAN_ARK_CODING;
 * System.out.println(platform.getPlatformName());  // "火山方舟-代码"
 * System.out.println(platform.getBaseUrl());       // "https://ark.cn-beijing.volces.com"
 * System.out.println(platform.getChatPath());      // "/api/coding/v3/chat/completions"
 *
 * // 2. 在 LLMConfig 中使用
 * LLMConfig config = new LLMConfig();
 * config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
 * config.setApiKey("your-api-key");
 * config.setModel("doubao-seed-2.0-lite");
 *
 * // 3. 遍历所有平台
 * for (LLMPlatformEnum platform : LLMPlatformEnum.values()) {
 *     System.out.println(platform.getPlatformName() + ": " + platform.getBaseUrl());
 * }
 * }</pre>
 *
 * <h3>API 兼容性说明：</h3>
 * <p>
 * 所有平台均采用 OpenAI 兼容的 API 格式，可以使用统一的调用方式。
 * 主要差异在于：
 * </p>
 * <ul>
 *   <li>BASE_URL - 各平台的 API 基础地址不同</li>
 *   <li>CHAT_PATH - 对话接口的路径可能不同</li>
 *   <li>认证方式 - 大多使用 Bearer Token，部分平台有额外要求</li>
 *   <li>模型命名 - 各平台的模型名称规则不同</li>
 * </ul>
 *
 * @author OWL Team
 * @version 1.0
 * @see LLMConfig
 * @see LLMClient
 * @since 2026-04-16
 */
@Getter
public enum LLMPlatformEnum {

    // ==================== 火山方舟（字节豆包）====================

    /**
     * 火山方舟 - 通用对话模型
     * <p>
     * 字节跳动推出的通用大语言模型平台，适合日常对话、文本生成等场景。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 中文理解能力强</li>
     *   <li>✅ 响应速度快</li>
     *   <li>✅ 性价比高</li>
     *   <li>✅ 支持长上下文</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>doubao-seed-2.0-lite - 轻量级，快速响应</li>
     *   <li>doubao-seed-2.0-pro - 专业级，更强性能</li>
     *   <li>doubao-1.5-pro - 上一代主力模型</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://console.volcengine.com/ark">火山引擎控制台</a>
     */
    HUOSHAN_ARK_GENERAL(
            "火山方舟-通用",
            "https://ark.cn-beijing.volces.com",
            "/api/v3/chat/completions"
    ),

    /**
     * 火山方舟 - 代码大模型
     * <p>
     * 专为代码生成、理解和优化训练的大模型，适合编程辅助场景。
     * 当前项目主要使用的平台。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 代码生成能力强</li>
     *   <li>✅ 支持多种编程语言</li>
     *   <li>✅ 代码解释清晰</li>
     *   <li>✅ Bug 修复建议准确</li>
     * </ul>
     *
     * <h3>适用场景：</h3>
     * <ul>
     *   <li>代码自动生成</li>
     *   <li>代码审查和优化</li>
     *   <li>Bug 诊断和修复</li>
     *   <li>技术文档生成</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>doubao-seed-2.0-lite - 快速代码补全</li>
     *   <li>doubao-seed-2.0-pro - 复杂代码生成</li>
     *   <li>doubao-seed-1-6-250615 - 旧版稳定模型</li>
     * </ul>
     */
    HUOSHAN_ARK_CODING(
            "火山方舟-代码",
            "https://ark.cn-beijing.volces.com",
            "/api/coding/v3/chat/completions"
    ),

    // ==================== 百度千帆（文心一言）====================

    /**
     * 百度千帆 - 文心一言
     * <p>
     * 百度推出的大语言模型平台，中文处理能力突出，适合中文场景应用。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 中文理解能力极强</li>
     *   <li>✅ 知识图谱丰富</li>
     *   <li>✅ 本土化服务好</li>
     *   <li>✅ 企业级稳定性</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>ernie-bot-4 - 最新一代文心大模型</li>
     *   <li>ernie-bot-8k - 支持 8K 上下文</li>
     *   <li>ernie-bot-turbo - 快速响应版本</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://console.bce.baidu.com/qianfan">百度千帆控制台</a>
     */
    BAIDU_QIANFAN(
            "百度千帆-文心",
            "https://qianfan.baidubce.com",
            "/v2/chat/completions"
    ),

    // ==================== 阿里云通义千问 ====================

    /**
     * 阿里云通义千问
     * <p>
     * 阿里巴巴推出的大语言模型平台，多语言能力和逻辑推理能力强。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 多语言支持优秀</li>
     *   <li>✅ 逻辑推理能力强</li>
     *   <li>✅ 数学计算准确</li>
     *   <li>✅ 阿里生态整合</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>qwen-max - 最强性能版本</li>
     *   <li>qwen-plus - 平衡性能和成本</li>
     *   <li>qwen-turbo - 快速响应版本</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://dashscope.console.aliyun.com">阿里云百炼控制台</a>
     */
    ALI_DASHSCOPE(
            "阿里云通义",
            "https://dashscope.aliyuncs.com",
            "/compatible-mode/v1/chat/completions"
    ),

    // ==================== 腾讯混元 ====================

    /**
     * 腾讯混元
     * <p>
     * 腾讯推出的大语言模型平台，与腾讯生态深度整合。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 腾讯生态整合</li>
     *   <li>✅ 社交场景优化</li>
     *   <li>✅ 内容安全过滤</li>
     *   <li>✅ 企业级服务</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>hunyuan-standard - 标准版</li>
     *   <li>hunyuan-pro - 专业版</li>
     *   <li>hunyuan-lite - 轻量版</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://console.cloud.tencent.com/hunyuan">腾讯云混元控制台</a>
     */
    TENCENT_HUNYUAN(
            "腾讯混元",
            "https://api.hunyuan.cloud.tencent.com",
            "/v1/chat/completions"
    ),

    // ==================== 智谱AI（GLM）====================

    /**
     * 智谱 AI - GLM 大模型
     * <p>
     * 智谱 AI 推出的 GLM 系列大模型，开源友好，学术背景强。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 开源模型可用</li>
     *   <li>✅ 学术研究支持</li>
     *   <li>✅ 多语言能力</li>
     *   <li>✅ 工具调用能力强</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>glm-4 - 最新一代 GLM 模型</li>
     *   <li>glm-3-turbo - 快速响应版本</li>
     *   <li>chatglm3-6b - 可本地部署的开源模型</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://open.bigmodel.cn">智谱 AI 开放平台</a>
     */
    ZHIPU_GLM(
            "智谱GLM",
            "https://open.bigmodel.cn",
            "/api/paas/v4/chat/completions"
    ),

    // ==================== Kimi（月之暗面）====================

    /**
     * Kimi - 月之暗面
     * <p>
     * 月之暗面推出的 Kimi 智能助手，以超长上下文窗口著称。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 超长上下文（200K+）</li>
     *   <li>✅ 文档处理能力强</li>
     *   <li>✅ 信息提取准确</li>
     *   <li>✅ 免费额度充足</li>
     * </ul>
     *
     * <h3>适用场景：</h3>
     * <ul>
     *   <li>长文档分析和总结</li>
     *   <li>多文件对比处理</li>
     *   <li>学术论文阅读</li>
     *   <li>法律合同审查</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>moonshot-v1-8k - 8K 上下文</li>
     *   <li>moonshot-v1-32k - 32K 上下文</li>
     *   <li>moonshot-v1-128k - 128K 上下文</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://platform.moonshot.cn">月之暗面开放平台</a>
     */
    MOONSHOT_KIMI(
            "Kimi",
            "https://api.moonshot.cn",
            "/v1/chat/completions"
    ),

    // ==================== DeepSeek ====================

    /**
     * DeepSeek - 深度求索
     * <p>
     * 深度求索推出的大语言模型，以高性价比和优秀性能著称。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 性价比极高</li>
     *   <li>✅ 代码能力强</li>
     *   <li>✅ 数学推理优秀</li>
     *   <li>✅ 响应速度快</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>deepseek-chat - 通用对话模型</li>
     *   <li>deepseek-coder - 代码专用模型</li>
     *   <li>deepseek-reasoner - 推理增强模型</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://platform.deepseek.com">DeepSeek 开放平台</a>
     */
    DEEPSEEK(
            "DeepSeek",
            "https://api.deepseek.com",
            "/v1/chat/completions"
    ),

    // ==================== MiniMax ====================

    /**
     * MiniMax
     * <p>
     * MiniMax 推出的大语言模型平台，对话体验和多模态能力强。
     * </p>
     *
     * <h3>特点：</h3>
     * <ul>
     *   <li>✅ 对话自然流畅</li>
     *   <li>✅ 多模态支持</li>
     *   <li>✅ 角色扮演能力强</li>
     *   <li>✅ 情感理解细腻</li>
     * </ul>
     *
     * <h3>适用场景：</h3>
     * <ul>
     *   <li>智能客服</li>
     *   <li>虚拟助手</li>
     *   <li>角色扮演</li>
     *   <li>情感陪伴</li>
     * </ul>
     *
     * <h3>常用模型：</h3>
     * <ul>
     *   <li>MiniMax-M2.7 - 最新一代模型</li>
     *   <li>abab6.5 - 上一代主力模型</li>
     *   <li>abab5.5 - 经济型模型</li>
     * </ul>
     *
     * <h3>控制台：</h3>
     * <a href="https://platform.minimaxi.com">MiniMax 开放平台</a>
     */
    MINIMAX(
            "MiniMax",
            "https://api.minimaxi.com",
            "/v1/text/chatcompletion_v2"
    );

    // ==================== 枚举属性 ====================

    /**
     * 平台名称
     * <p>
     * 用于显示和识别的中文名称，便于用户理解。
     * </p>
     */
    private final String platformName;

    /**
     * API 基础地址
     * <p>
     * LLM 平台的 API 服务器地址，所有请求都基于此 URL。
     * 完整的 API 地址 = baseUrl + chatPath
     * </p>
     *
     * @see #chatPath
     */
    private final String baseUrl;

    /**
     * 对话接口路径
     * <p>
     * 聊天完成接口的相对路径，拼接到 baseUrl 后形成完整 API 地址。
     * 完整的 API 地址 = baseUrl + chatPath
     * </p>
     *
     * <h3>示例：</h3>
     * <pre>{@code
     * baseUrl = "https://ark.cn-beijing.volces.com"
     * chatPath = "/api/coding/v3/chat/completions"
     * 完整地址 = "https://ark.cn-beijing.volces.com/api/coding/v3/chat/completions"
     * }</pre>
     *
     * @see #baseUrl
     */
    private final String chatPath;

    // ==================== 构造方法 ====================

    /**
     * 私有构造方法
     * <p>
     * 初始化枚举实例的平台配置信息。
     * 枚举实例在类加载时创建，线程安全且单例。
     * </p>
     *
     * @param platformName 平台显示名称
     * @param baseUrl API 基础地址
     * @param chatPath 对话接口路径
     */
    LLMPlatformEnum(String platformName, String baseUrl, String chatPath) {
        this.platformName = platformName;
        this.baseUrl = baseUrl;
        this.chatPath = chatPath;
    }
}