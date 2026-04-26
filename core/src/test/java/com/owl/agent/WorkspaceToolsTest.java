package com.owl.agent;

import com.owl.core.llm.*;
import com.owl.core.skills.tools.WorkspaceTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkspaceTools 测试 - 使用大模型调度
 */
@DisplayName("WorkspaceTools 测试 - 使用大模型调度")
class WorkspaceToolsTest {

    private static final String TEST_USER_ID = "test";
    private static final String API_KEY = "test";
    private static final String WORKSPACE_URL = "http://localhost:8000";

    private LLMClient llmClient;
    private WorkspaceTools workspaceTools;

    @BeforeEach
    void setUp() {
        workspaceTools = new WorkspaceTools(WORKSPACE_URL, API_KEY);

        LLMConfig config = new LLMConfig();
        config.setLlmPlatform(LLMPlatformEnum.HUOSHAN_ARK_CODING);
        config.setApiKey("fb5c4b70-abb0-4e0b-b390-138ad84c505a");
        config.setModel("Doubao-Seed-2.0-pro");
        config.setTemperature(0.2);
        llmClient = LLMClient.create(config);
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_create 创建脚本")
    void testCreateUserScriptWithLLM() {
        String scriptName = "llm_create_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请调用 workspace_script_create 工具创建 Python 脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: print("Hello from LLM created script")
                
                请先调用工具完成创建，然后返回创建结果。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("createUserScript 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_list 列出脚本")
    void testListUserScriptsWithLLM() {
        String prompt = """
                请调用 workspace_script_list 工具列出当前用户的所有脚本。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("listUserScripts 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_execute 执行脚本")
    void testExecuteUserScriptWithLLM() {
        String scriptName = "llm_exec_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请先调用 workspace_script_create 创建脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: print("Script executed by LLM test")
                
                然后调用 workspace_script_execute 执行该脚本。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("executeUserScript 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_update 更新脚本")
    void testUpdateUserScriptWithLLM() {
        String scriptName = "llm_update_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请先调用 workspace_script_create 创建脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: print("original")
                
                然后调用 workspace_script_update 更新该脚本:
                - 新内容: print("Updated by LLM test")
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("updateUserScript 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_delete 删除脚本")
    void testDeleteUserScriptWithLLM() {
        String scriptName = "llm_delete_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请先调用 workspace_script_create 创建脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: print("to be deleted")
                
                然后调用 workspace_script_delete 删除该脚本。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("deleteUserScript 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_script_get_content 获取脚本内容")
    void testGetUserScriptContentWithLLM() {
        String scriptName = "llm_content_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请先调用 workspace_script_create 创建脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: def hello():\n    print("Hello from content test")
                
                然后调用 workspace_script_get_content 获取该脚本的内容。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("getUserScriptContent 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_space_info 获取空间信息")
    void testGetUserSpaceInfoWithLLM() {
        String prompt = """
                请调用 workspace_space_info 工具获取当前用户的工作空间信息。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("getUserSpaceInfo 响应: " + response.content());
    }

    @Test
    @DisplayName("测试使用大模型调用 workspace_space_create 创建用户空间")
    void testCreateUserSpaceWithLLM() {
        String newUserId = "llm_user_" + (System.currentTimeMillis() % 10000);

        String prompt = """
                请调用 workspace_space_create 工具为当前用户创建工作空间。
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(newUserId))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("createUserSpace 响应: " + response.content());
    }

    @Test
    @DisplayName("测试完整脚本管理工作流")
    void testCompleteScriptWorkflowWithLLM() {
        String scriptName = "llm_workflow_" + System.currentTimeMillis() + ".py";

        String workflowPrompt = """
                请执行以下脚本管理操作序列:
                
                1. 创建脚本 '""" + scriptName + """
                '，内容: print("Workflow test - Created")
                2. 列出当前用户的所有脚本，确认新脚本存在
                3. 获取该脚本的内容，确认创建成功
                4. 更新脚本内容为: print("Workflow test - Updated")
                5. 执行该脚本，确认更新后的内容被正确执行
                
                请按顺序执行并报告每一步的结果。
                """;

        LLMAgentResponse response = llmClient.chat(workflowPrompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("完整工作流响应: " + response.content());
    }

    @Test
    @DisplayName("测试脚本执行带参数")
    void testExecuteScriptWithArgsWithLLM() {
        String scriptName = "llm_args_" + System.currentTimeMillis() + ".py";
        String prompt = """
                请先调用 workspace_script_create 创建脚本:
                - 脚本名称: """ + scriptName + """
                - 内容: import sys\nprint("Args:", sys.argv)
                
                然后调用 workspace_script_execute 执行该脚本，传递参数: ["hello", "world"]
                """;

        LLMAgentResponse response = llmClient.chat(prompt)
                .userMetadata(UserMetadata.of(TEST_USER_ID))
                .tool(workspaceTools)
                .call();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("executeUserScript 带参数响应: " + response.content());
    }
}
