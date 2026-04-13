package com.owl.core.llm;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class ChatModelFactory {

    public static class Builder {
        private String apiKey;
        private String model = "gpt-4o";
        private String baseUrl = "https://api.openai.com/v1";

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public ChatModel build() {
            var api = new OpenAiApi.Builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

            return OpenAiChatModel.builder()
                .defaultOptions(OpenAiChatOptions.builder()
                    .model(model)
                    .build())
                .openAiApi(api)
                .build();
        }
    }

    public static ChatModel buildChatModel(Builder builder) {
        return builder.build();
    }
}