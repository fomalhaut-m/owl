package com.owl.core.llm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MiniMax API 测试")
class MiniMaxTest {

    @Test
    @DisplayName("MiniMax API 调用")
    void testMiniMaxChat() {
        String apiKey = "sk-cp-0cx8-H-KKo14uqdNurVEZFw_U2KRjadkIGl3c41wfSVge75_ZE-v9GJHhtRyxZD96_l2461T8bK8KjGSRWwUj21Uhs_M1waHOZCuTViL3Vlvn10jh4iFPL0";

        var chatModel = new ChatModelFactory.Builder()
            .apiKey(apiKey)
            .model("MiniMax-M2.7")
            .baseUrl("https://api.minimaxi.com")
            .build();

        var client = new ChatClient(chatModel);

        var response = client.chat("Hello");

        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("Response: " + response);
    }
}