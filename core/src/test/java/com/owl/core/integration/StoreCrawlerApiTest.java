package com.owl.core.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 使用 store_crawler API 密钥的简单测试类
 */
public class StoreCrawlerApiTest {
    
    // 使用 store_crawler 的 API 密钥
    private static final String API_KEY = "sk-b9215e2cfd28436988f1f00abe26609f";
    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";
    
    public static void main(String[] args) {
        try {
            System.out.println("正在使用 store_crawler 密钥进行测试...");
            System.out.println("API Key: " + API_KEY.substring(0, 6) + "..." + API_KEY.substring(API_KEY.length() - 4));
            
            HttpClient client = HttpClient.newHttpClient();
            
            String requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "user",
                        "content": "你好，这是一个来自 Owl Code 测试的消息。请回复 OK 表示你收到了。"
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
            
            if (response.statusCode() == 200) {
                System.out.println("✅ store_crawler API 密钥测试成功！");
                System.out.println("响应内容: " + response.body());
            } else {
                System.out.println("❌ API 请求失败。状态码: " + response.statusCode());
                System.out.println("错误响应: " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("请求过程中发生错误: " + e.getMessage());
            System.out.println("这可能是由于网络连接问题导致的，如代理配置错误。");
        }
    }
}