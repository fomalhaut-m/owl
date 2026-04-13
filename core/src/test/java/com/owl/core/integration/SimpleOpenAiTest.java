package com.owl.core.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 简单的 OpenAI API 测试类，用于验证 API 密钥是否有效
 */
public class SimpleOpenAiTest {
    
    // 使用提供的 API 密钥
    private static final String API_KEY = "sk-6b8c3f1d2dfc4e70993707bc94352441"; // 雷鸣的密钥
    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";
    
    public static void main(String[] args) {
        try {
            // 发送一个简单的请求来测试 API 密钥
            HttpClient client = HttpClient.newHttpClient();
            
            String requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "user",
                        "content": "你好，请简单介绍一下你自己"
                    }
                ],
                "temperature": 0.7
            }
            """;
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
                
            System.out.println("正在发送请求到 OpenAI API...");
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
                
            System.out.println("响应状态码: " + response.statusCode());
            System.out.println("响应内容: " + response.body());
            
            if (response.statusCode() == 200) {
                System.out.println("\n✅ API 密钥测试成功！可以正常使用。");
            } else {
                System.out.println("\n❌ API 密钥测试失败。请检查密钥是否正确。");
            }
            
        } catch (Exception e) {
            System.err.println("请求过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}