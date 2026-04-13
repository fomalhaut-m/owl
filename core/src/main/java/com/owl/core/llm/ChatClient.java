package com.owl.core.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;

public class ChatClient {
    private static final Logger log = LoggerFactory.getLogger(ChatClient.class);
    private final ChatModel chatModel;

    public ChatClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        log.info("Sending message: {}", message);
        String response = chatModel.call(message);
        log.info("Received response: {}", response);
        return response;
    }

    public ChatModel getChatModel() {
        return chatModel;
    }
}