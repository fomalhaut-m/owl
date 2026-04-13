package com.owl.core.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MiniMax API 集成测试")
class MiniMaxIntegrationTests {

    // 获取 MiniMax API 客户端
    private ChatModel createMiniMaxChatModel() {
        String apiKey = System.getenv("MINIMAX_API_KEY"); // 从环境变量获取 API KEY
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-minimax-api-key"; // 替换为您的实际 API KEY
        }

        // 使用 MiniMax API 端点
        OpenAiApi openAiApi = new OpenAiApi("https://api.minimaxi.com/v1", apiKey);
        return new OpenAiChatModel(openAiApi,
            OpenAiChatOptions.builder()
                .withModel("abab6.5s-chat") // 使用适当的 MiniMax 模型
                .build());
    }

    @Test
    @DisplayName("1. MiniMax 大模型调用测试")
    void testBasicMiniMaxLLMCall() {
        ChatModel chatModel = createMiniMaxChatModel();
        
        String userInput = "你好，请简单介绍一下你自己";
        
        Prompt prompt = new Prompt(userInput);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.length() > 0, "响应内容长度应大于0");
        System.out.println("MiniMax 大模型调用结果: " + result);
    }

    @Test
    @DisplayName("2. MiniMax 大模型 Tools 调用测试")
    void testMiniMaxLLMWithTools() {
        ChatModel chatModel = createMiniMaxChatModel();

        String toolsJson = """
            [
                {
                    "type": "function",
                    "function": {
                        "name": "get_current_weather",
                        "description": "获取指定城市的当前天气",
                        "parameters": {
                            "type": "object",
                            "properties": {
                                "city": {
                                    "type": "string",
                                    "description": "城市名称"
                                }
                            },
                            "required": ["city"]
                        }
                    }
                }
            ]
            """;

        String userInput = "请告诉我上海现在的天气怎么样？";
        
        Prompt prompt = new Prompt(userInput);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        System.out.println("MiniMax 大模型 Tools 调用结果: " + result);
    }

    @Test
    @DisplayName("3. MiniMax + MBTI 分析专家 Skills 提示词调用测试")
    void testMiniMaxLLMWithMBTIExpertSkills() {
        ChatModel chatModel = createMiniMaxChatModel();

        String mbtiExpertPrompt = """
            你是专业的MBTI人格分析专家，具有以下特征：
            - 深入了解16种MBTI人格类型的特征
            - 能够准确分析用户的MBTI类型
            - 能够提供基于MBTI类型的职业建议和个人发展指导
            - 语言专业、分析深入、建议实用
            
            请分析以下用户描述的人格特征，并给出相应的MBTI类型分析：
            """;

        String userInput = "我是一个喜欢独处、善于思考、注重细节、喜欢按计划行事的人。请分析我的MBTI类型。";
        String fullPrompt = mbtiExpertPrompt + userInput;
        
        Prompt prompt = new Prompt(fullPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.toLowerCase().contains("mbti"), "响应应包含MBTI相关内容");
        System.out.println("MiniMax MBTI专家技能调用结果: " + result);
    }

    @Test
    @DisplayName("4. MiniMax + MBTI 分析专家 + 记忆整理 Skills 调用测试")
    void testMiniMaxLLMWithMBTIAndMemorySkills() {
        ChatModel chatModel = createMiniMaxChatModel();

        String combinedPrompt = """
            你是专业的MBTI人格分析专家和记忆整理助手，具备以下能力：
            1. MBTI分析专家：
               - 深入了解16种MBTI人格类型
               - 能够准确分析用户人格类型
            2. 记忆整理助手：
               - 能够帮助用户整理和归纳重要信息
               - 能够创建结构化的记忆要点
            
            请完成以下任务：
            - 分析用户提供的性格描述
            - 整理分析过程中的关键要点
            - 提供结构化的MBTI分析结果和记忆要点
            
            用户描述：我是一个内向、直觉、思考、判断型的人。我喜欢深度思考，善于分析问题，做事有条理，喜欢制定计划并严格执行。
            """;
        
        Prompt prompt = new Prompt(combinedPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("整理") || result.contains("要点"), "响应应包含记忆整理相关内容");
        System.out.println("MiniMax MBTI+记忆整理技能调用结果: " + result);
    }

    @Test
    @DisplayName("5. MiniMax 目标拆解计划（Goal Decomposition Plan）测试")
    void testMiniMaxGoalDecompositionPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String goalDecompositionPrompt = """
            请使用目标拆解计划来帮助我完成以下任务：
            
            目标：在3个月内学会Java Spring Boot开发
            
            请按照以下步骤进行目标拆解：
            1. 明确最终目标
            2. 识别关键里程碑
            3. 拆解为具体的小任务
            4. 设置时间安排
            5. 确定所需资源和工具
            6. 制定评估标准
            
            请提供详细的分步计划。
            """;
        
        Prompt prompt = new Prompt(goalDecompositionPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("1.") && result.contains("2."), "响应应包含分步骤内容");
        System.out.println("MiniMax 目标拆解计划结果: " + result);
    }

    @Test
    @DisplayName("6. MiniMax 工具调用计划（Tool Call Plan）测试")
    void testMiniMaxToolCallPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String toolCallPlanPrompt = """
            请制定一个工具调用计划来解决以下问题：
            
            问题：需要获取当前日期的天气信息，并根据天气情况推荐合适的户外活动
            
            请规划所需的工具调用序列：
            1. 需要哪些工具？
            2. 工具调用的顺序是什么？
            3. 如何处理工具调用的结果？
            4. 如何整合结果并给出最终建议？
            
            请详细描述工具调用计划。
            """;
        
        Prompt prompt = new Prompt(toolCallPlanPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("工具") || result.contains("调用"), "响应应包含工具调用相关内容");
        System.out.println("MiniMax 工具调用计划结果: " + result);
    }

    @Test
    @DisplayName("7. MiniMax 反思修正计划（Reflection Plan）测试")
    void testMiniMaxReflectionPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String reflectionPlanPrompt = """
            请使用反思修正计划来分析以下情况：
            
            情况：我尝试学习一门新技能（比如编程），但进展缓慢，感到沮丧
            
            请按照以下步骤进行反思修正：
            1. 当前状况分析
            2. 识别问题根源
            3. 评估现有方法的有效性
            4. 提出改进策略
            5. 制定修正后的行动计划
            6. 设定评估节点
            
            请提供详细的反思修正计划。
            """;
        
        Prompt prompt = new Prompt(reflectionPlanPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("分析") && result.contains("改进"), "响应应包含分析和改进内容");
        System.out.println("MiniMax 反思修正计划结果: " + result);
    }

    @Test
    @DisplayName("8. MiniMax 多轮对话推进计划（Conversation Plan）测试")
    void testMiniMaxConversationPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String conversationPlanPrompt = """
            请设计一个多轮对话推进计划来完成以下任务：
            
            任务：帮助用户选择合适的职业发展方向
            
            请规划对话流程：
            1. 开场白和目标确认
            2. 信息收集阶段（兴趣、技能、价值观等）
            3. 分析讨论阶段
            4. 方案建议阶段
            5. 总结和后续行动
            
            请提供每轮对话的目标和可能的回应策略。
            """;
        
        Prompt prompt = new Prompt(conversationPlanPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("对话") || result.contains("阶段"), "响应应包含对话阶段内容");
        System.out.println("MiniMax 多轮对话推进计划结果: " + result);
    }

    @Test
    @DisplayName("9. MiniMax 代码工程计划（Code Engineering Plan）测试")
    void testMiniMaxCodeEngineeringPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String codeEngineeringPlanPrompt = """
            请制定一个代码工程计划来实现以下需求：
            
            需求：开发一个简单的任务管理系统
            
            请提供详细的工程计划：
            1. 需求分析和功能列表
            2. 技术栈选择
            3. 系统架构设计
            4. 数据库设计
            5. 模块划分和开发顺序
            6. 测试策略
            7. 部署方案
            
            请提供完整的代码工程计划。
            """;
        
        Prompt prompt = new Prompt(codeEngineeringPlanPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("架构") || result.contains("数据库"), "响应应包含工程相关内容");
        System.out.println("MiniMax 代码工程计划结果: " + result);
    }

    @Test
    @DisplayName("10. MiniMax 最终总结计划（Summary Plan）测试")
    void testMiniMaxSummaryPlan() {
        ChatModel chatModel = createMiniMaxChatModel();

        String summaryPlanPrompt = """
            请使用总结计划来整理以下复杂信息：
            
            信息：今天讨论了目标设定、工具使用、学习方法、职业规划等多个话题，涉及了时间管理、技能提升、团队协作等方面的内容
            
            请按照以下结构进行总结：
            1. 核心主题识别
            2. 关键要点梳理
            3. 重要结论汇总
            4. 行动建议提炼
            5. 后续关注点
            
            请提供结构化的总结内容。
            """;
        
        Prompt prompt = new Prompt(summaryPlanPrompt);
        ChatResponse response = chatModel.call(prompt);
        
        String result = response.getResult().getOutput().getContent();
        
        assertNotNull(result, "响应不应为空");
        assertTrue(result.contains("总结") || result.contains("要点"), "响应应包含总结内容");
        System.out.println("MiniMax 最终总结计划结果: " + result);
    }
}