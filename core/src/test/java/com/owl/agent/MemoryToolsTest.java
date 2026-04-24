package com.owl.agent;

import com.owl.core.llm.LLMClient;
import com.owl.core.llm.LLMConfig;
import com.owl.core.llm.LLMPlatformEnum;
import com.owl.core.llm.LLMAgentResponse;
import com.owl.core.memory.FileMemoryRepo;
import com.owl.core.memory.MemoryMetadata;
import com.owl.core.skills.tools.MemoryTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryTools 测试 - 使用大模型")
class MemoryToolsTest {

    private static final String TEST_USER_ID = "user_001";
    private static final String API_KEY = "fb5c4b70-abb0-4e0b-b390-138ad84c505a";
    
    private LLMClient llmClient;
    private MemoryTools memoryTools;
    private FileMemoryRepo memoryRepo;

    @BeforeEach
    void setUp() {
        memoryRepo = new FileMemoryRepo();
        memoryTools = new MemoryTools(memoryRepo);
        
        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey(API_KEY);
        config.setModel("Doubao-Seed-2.0-pro");
        config.setTemperature(0.2);
        llmClient = LLMClient.create(config);
    }

    @Test
    @DisplayName("测试使用大模型调用 saveMemory 保存记忆")
    void testSaveMemoryWithLLM() {
        String prompt = """
            请调用 saveMemory 工具保存以下信息:
            - 路径: working/P0/test_note.md
            - 内容: 这是一条测试记忆，用于验证 MemoryTools 的保存功能。创建时间: 2026-04-23
            
            请先调用工具完成保存，然后返回保存结果。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("saveMemory 响应: " + response.content());
        
        String savedContent = memoryRepo.getMemory(TEST_USER_ID, "working/P0/test_note.md");
        assertNotNull(savedContent);
        assertTrue(savedContent.contains("测试记忆"));
    }

    @Test
    @DisplayName("测试使用大模型调用 getMemory 读取记忆")
    void testGetMemoryWithLLM() {
        memoryRepo.saveMemory(TEST_USER_ID, "working/P1/read_test.md", 
                "这是用于测试读取的记忆内容。包含重要信息: 测试成功。");

        String prompt = """
            请调用 getMemory 工具读取以下路径的记忆:
            - 路径: working/P1/read_test.md
            
            请返回读取到的内容。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("getMemory 响应: " + response.content());
        assertTrue(response.content().contains("测试读取的记忆"));
    }

    @Test
    @DisplayName("测试使用大模型调用 deleteMemory 删除记忆")
    void testDeleteMemoryWithLLM() {
        memoryRepo.saveMemory(TEST_USER_ID, "working/P2/delete_test.md", 
                "这是将要被删除的记忆。");

        String prompt = """
            请调用 deleteMemory 工具删除以下路径的记忆:
            - 路径: working/P2/delete_test.md
            
            请返回��除操作的结果。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("deleteMemory 响应: " + response.content());
        
        String deletedContent = memoryRepo.getMemory(TEST_USER_ID, "working/P2/delete_test.md");
        assertNull(deletedContent);
    }

    @Test
    @DisplayName("测试使用大模型调用 getMemoryPaths 获取所有记忆路径")
    void testGetMemoryPathsWithLLM() {
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/path_test_1.md", "记忆1");
        memoryRepo.saveMemory(TEST_USER_ID, "working/P1/path_test_2.md", "记忆2");
        memoryRepo.saveMemory(TEST_USER_ID, "long_term/S/path_test_3.md", "记忆3");

        String prompt = """
            请调用 getMemoryPaths 工具获取当前用户的所有记忆路径。
            
            请列出返回的所有记忆路径。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("getMemoryPaths 响应: " + response.content());
        assertTrue(response.content().contains("path_test"));
    }

    @Test
    @DisplayName("测试使用大模型调用 getMemoryPaths 获取指定时间范围内的记忆")
    void testGetMemoryPathsByTimeRangeWithLLM() {
        long now = System.currentTimeMillis();
        long oneDayAgo = now - 24 * 60 * 60 * 1000;
        long oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000;

        String prompt = """
            请调用 getMemoryPathsByTimeRange 工具获取指定时间范围内的记忆:
            - 起始时间: """ + oneWeekAgo + """
              (一周前)
            - 结束时间: """ + now + """
              (当前)
            
            请返回符合条件的所有记忆路径。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("getMemoryPaths 时间范围响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型进行记忆巩固")
    void testMemoryConsolidationWithLLM() {
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/recent_thought.md", 
                "今天学到了关于 Java 泛型的新知识。泛型可以提供编译时类型安全。");
        memoryRepo.saveMemory(TEST_USER_ID, "long_term/S/java_generics.md", 
                "Java 泛型学习笔记。类型通配符: <? extends T>, <? super T>。");

        String consolidationPrompt = """
            # Dream: Memory Consolidation
            
            你正在执行一个 dream —— 对你的记忆文件进行反思性处理。
            将你最近学到的内容综合成持久、组织良好的记忆。
            
            记忆目录：`mem_system/users/user_001/mnemosyne`
            
            请完成以下任务:
            1. 查看现有记忆结构
            2. 检查是否有需要合并的新信息
            3. 如果有重复内容，进行整合
            
            最后请返回你完成的操作摘要。
            """;

        LLMAgentResponse response = llmClient.chat(
                consolidationPrompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("记忆巩固响应: " + response.content());
    }

    @Test
    @DisplayName("测试多用户记忆隔离")
    void testMultiUserMemoryIsolation() {
        String testUserId = "user_002";
        FileMemoryRepo user2Repo = new FileMemoryRepo();
        
        user2Repo.saveMemory(testUserId, "working/P0/user2_note.md", 
                "这是 user_002 的专属记忆。");
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/user1_note.md", 
                "这是 user_001 的专属记忆。");

        String prompt = """
            请检查 user_001 和 user_002 的记忆是否正确隔离。
            
            检查 user_001 的记忆中是否有 "user_002" 字样（不应该有）。
            检查 user_002 的记忆中是否有 "user_001" 字样（不应该有）。
            
            请返回检查结果。
            """;

        LLMAgentResponse response = llmClient.chat(
                prompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("多用户隔离测试响应: " + response.content());
    }

    @Test
    @DisplayName("测试完整记忆管理工作流")
    void testCompleteMemoryWorkflow() {
        String workflowPrompt = """
            请执行以下记忆管理操作序列:
            
            1. 保存一条新记忆到 working/P1/workflow_test.md，内容为: "工作流程测试 - 创建于 2026-04-23"
            2. 读取该记忆确认保存成功
            3. 获取所有记忆路径列表
            4. 更新该记忆内容为: "工作流程测试 - 已更新于 2026-04-23"
            5. 再次读取确认更新成功
            
            请按顺序执行并报告每一步的结果。
            """;

        LLMAgentResponse response = llmClient.chat(
                workflowPrompt,
                Collections.emptyList(),
                memoryTools
        );

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("完整工作流响应: " + response.content());
        
        String finalContent = memoryRepo.getMemory(TEST_USER_ID, "working/P1/workflow_test.md");
        assertNotNull(finalContent);
        assertTrue(finalContent.contains("已更新"));
    }
}