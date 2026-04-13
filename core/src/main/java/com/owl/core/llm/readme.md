# LLM 领域

LLM 领域负责管理大模型对接，提供 ChatClient。

## 模块结构

```
llm/
├── ChatClient.java    # 聊天客户端（依赖 spring-ai）
└── ChatModelFactory.java # ChatModel 工厂
```

## 关键设计

### 1. ChatClient

```java
public class ChatClient {
    private final ChatModel chatModel;

    public ChatClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        var prompt = new Prompt(message);
        var response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
```

### 2. ChatModel 工厂

```java
public class ChatModelFactory {
    public static ChatModel createOpenAi(String apiKey, String model) {
        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(model)
            .build();
    }

    public static ChatModel createAnthropic(String apiKey, String model) {
        return AnthropicChatModel.builder()
            .apiKey(apiKey)
            .modelName(model)
            .build();
    }
}
```

### 3. 支持的模型

| 模型 | 工厂方法 |
|------|----------|
| OpenAI | `createOpenAi()` |
| Anthropic (Claude) | `createAnthropic()` |

## 使用示例

```java
// 创建 OpenAI ChatModel
var chatModel = ChatModelFactory.createOpenAi(
    System.getenv("OPENAI_API_KEY"),
    "gpt-4"
);

// 创建 ChatClient
var client = new ChatClient(chatModel);

// 对话
var response = client.chat("Hello");
System.out.println(response);
```

## 实现顺序

1. ChatClient 类
2. ChatModelFactory 工厂类
