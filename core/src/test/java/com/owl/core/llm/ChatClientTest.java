package com.owl.core.llm;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChatClient 测试")
class ChatClientTest {

    @Test
    @DisplayName("构造函数")
    void testChatClientConstructor() {
        var client = new ChatClient(null);
        assertNotNull(client);
    }

    @Test
    @DisplayName("获取 ChatModel")
    void testGetChatModel() {
        var client = new ChatClient(null);
        assertNull(client.getChatModel());
    }
}