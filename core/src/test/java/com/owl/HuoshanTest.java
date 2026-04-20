package com.owl;

import com.owl.core.skills.tools.TimeTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@DisplayName("火山引擎 API 测试")
class HuoshanTest {

    String apiKey = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";

    @Test
    @DisplayName("火山引擎 API 调用")
    void testHuoshanChat() {

        ChatModel model = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(apiKey)
                        .baseUrl("https://ark.cn-beijing.volces.com")
                        .restClientBuilder(restClientBuilder())
                        .completionsPath("/api/coding/v3/chat/completions")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("doubao-seed-2.0-lite")
                        .temperature(0.2d)
                        // .maxTokens(1024)
                        .build())
                .build();

        ChatClient client = ChatClient.builder(model).build();

        System.out.println(client.prompt("Hi, How are you ").call().content());

    }

    private RestClient.Builder restClientBuilder() {
        java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))  // 连接超时30秒
                .version(java.net.http.HttpClient.Version.HTTP_2);  // 使用HTTP/2

        java.net.http.HttpClient httpClient = httpClientBuilder.build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    return execution.execute(request, body);
                });
    }


    @Test
    @DisplayName("火山引擎测试 tools")
    void testHuoshanChatTools() {
        String apiKey = "sk-cp-0cx8-H-KKo14uqdNurVEZFw_U2KRjadkIGl3c41wfSVge75_ZE-v9GJHhtRyxZD96_l2461T8bK8KjGSRWwUj21Uhs_M1waHOZCuTViL3Vlvn10jh4iFPL0";

        ChatModel model = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(apiKey)
                        .baseUrl("https://ark.cn-beijing.volces.com")
                        .restClientBuilder(restClientBuilder())
                        .completionsPath("/api/coding/v3/chat/completions")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("doubao-seed-1-6-250615")
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