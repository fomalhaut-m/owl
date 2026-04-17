package com.owl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.owl.core.llm.LLMPlatformEnum;
import com.owl.core.tools.TimeTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

@DisplayName("MiniMax API 测试")
class MiniMaxTest {

    String apiKey = "sk-cp-0cx8-H-KKo14uqdNurVEZFw_U2KRjadkIGl3c41wfSVge75_ZE-v9GJHhtRyxZD96_l2461T8bK8KjGSRWwUj21Uhs_M1waHOZCuTViL3Vlvn10jh4iFPL0";
    @Test
    @DisplayName("MiniMax API 调用")
    void testMiniMaxChat() {

        ChatModel model = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(apiKey)
                        .baseUrl(LLMPlatformEnum.MINIMAX.getBaseUrl())
                        .completionsPath(LLMPlatformEnum.MINIMAX.getChatPath())
                        // .restClientBuilder(restClientBuilder(llmConfig.getProxyDefinition()))
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .temperature(0.2d)
                        // .maxTokens(1024)
                        .build())
                .build();

        ChatClient client = ChatClient.builder(model).build();

        System.out.println(client.prompt("Hi, How are you ").call().content());

    }


    @Test
    @DisplayName("MiniMax 测试 tools")
    void testMiniMaxChatTools() {

        ChatModel model = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(apiKey)
                        .baseUrl("https://api.minimaxi.com")
                        // .restClientBuilder(restClientBuilder(llmConfig.getProxyDefinition()))
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .temperature(0.2d)
                        // .maxTokens(1024)
                        .build())
                .build();

        ToolCallback[] callbacks = ToolCallbacks.from(new TimeTools());

        ChatClient client = ChatClient.builder(model)
                .defaultToolCallbacks(callbacks)
                .build();


        System.out.println(client.prompt("当前系统时间是? 当前上海的, 纽约的时间是?").call().content());

    }
}