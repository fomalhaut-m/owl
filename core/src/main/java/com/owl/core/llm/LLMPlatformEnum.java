package com.owl.core.llm;

import lombok.Getter;

/**
 * 国内主流AI大模型平台枚举
 * 封装：平台名称、BASE_URL、CHAT_COMPLETION_PATH、默认模型名
 */
@Getter
public enum LLMPlatformEnum {

    // ==================== 火山方舟（字节豆包）====================
    /**
     * 火山方舟-通用对话模型
     */
    HUOSHAN_ARK_GENERAL(
            "火山方舟-通用",
            "https://ark.cn-beijing.volces.com",
            "/api/v3/chat/completions"
    ),
    /**
     * 火山方舟-代码大模型（你当前使用的）
     */
    HUOSHAN_ARK_CODING(
            "火山方舟-代码",
            "https://ark.cn-beijing.volces.com",
            "/api/coding/v3/chat/completions"
    ),

    // ==================== 百度千帆（文心一言）====================
    BAIDU_QIANFAN(
            "百度千帆-文心",
            "https://qianfan.baidubce.com",
            "/v2/chat/completions"
    ),

    // ==================== 阿里云通义千问 ====================
    ALI_DASHSCOPE(
            "阿里云通义",
            "https://dashscope.aliyuncs.com",
            "/compatible-mode/v1/chat/completions"
    ),

    // ==================== 腾讯混元 ====================
    TENCENT_HUNYUAN(
            "腾讯混元",
            "https://api.hunyuan.cloud.tencent.com",
            "/v1/chat/completions"
    ),

    // ==================== 智谱AI（GLM）====================
    ZHIPU_GLM(
            "智谱GLM",
            "https://open.bigmodel.cn",
            "/api/paas/v4/chat/completions"
    ),

    // ==================== Kimi（月之暗面）====================
    MOONSHOT_KIMI(
            "Kimi",
            "https://api.moonshot.cn",
            "/v1/chat/completions"
    ),

    // ==================== DeepSeek ====================
    DEEPSEEK(
            "DeepSeek",
            "https://api.deepseek.com",
            "/v1/chat/completions"
    ),
    // ==================== MiniMax（api.minimaxi.com）====================
    MINIMAX(
            "MiniMax",
            "https://api.minimaxi.com",
            "/v1/text/chatcompletion_v2"
    ),
    ;

    // 枚举属性
    private final String platformName;    // 平台名称
    private final String baseUrl;         // 基础地址
    private final String chatPath;        // 对话接口路径

    // 构造方法
    LLMPlatformEnum(String platformName, String baseUrl, String chatPath) {
        this.platformName = platformName;
        this.baseUrl = baseUrl;
        this.chatPath = chatPath;
    }
}