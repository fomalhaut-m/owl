package com.owl.core.tools;

import com.owl.core.work.WorkspaceClient;
import com.owl.core.work.response.*;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 工作空间管理工具
 */
@Component
public class WorkspaceTools implements ToolComponent {

    private final WorkspaceClient client;

    public WorkspaceTools(
            @Value("${owl.workspace.url:http://localhost:8000}") String workspaceUrl,
            @Value("${owl.workspace.api-key:#{null}}") String apiKey) {
        this.client = new WorkspaceClient(workspaceUrl, apiKey);
    }

    @Tool(name = "workspace_script_execute", description = "执行用户的 Python 脚本")
    public ApiResponse<ScriptResult> executeUserScript(ToolContext context,
                                                      @ToolParam(description = "脚本名称") String name,
                                                      @ToolParam(description = "命令行参数", required = false) List<String> args,
                                                      @ToolParam(description = "超时时间(秒)", required = false) Integer timeout) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.executeUserScript(userId, name, args, timeout != null ? timeout : 30);
    }

    @Tool(name = "workspace_script_list", description = "列出用户的所有 Python 脚本")
    public ApiResponse<List<UserScriptInfo>> listUserScripts(ToolContext context) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.listUserScripts(userId);
    }

    @Tool(name = "workspace_script_create", description = "创建新的 Python 脚本")
    public ApiResponse<UserScriptInfo> createUserScript(ToolContext context,
                                                       @ToolParam(description = "脚本名称(必须以.py结尾)") String name,
                                                       @ToolParam(description = "脚本内容") String content) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        client.createUserSpace(userId);
        return client.createUserScript(userId, name, content);
    }

    @Tool(name = "workspace_script_update", description = "更新已有的 Python 脚本")
    public ApiResponse<UserScriptInfo> updateUserScript(ToolContext context,
                                                        @ToolParam(description = "脚本名称") String name,
                                                        @ToolParam(description = "新的脚本内容") String content) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.updateUserScript(userId, name, content);
    }

    @Tool(name = "workspace_script_delete", description = "删除指定的 Python 脚本")
    public ApiResponse<Map<String, String>> deleteUserScript(ToolContext context,
                                                              @ToolParam(description = "脚本名称") String name) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.deleteUserScript(userId, name);
    }

    @Tool(name = "workspace_script_get_content", description = "获取脚本的源代码内容")
    public ApiResponse<ScriptContentResponse> getUserScriptContent(ToolContext context,
                                                                   @ToolParam(description = "脚本名称") String name) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.getUserScriptContent(userId, name);
    }

    @Tool(name = "workspace_space_info", description = "获取用户工作空间的统计信息")
    public ApiResponse<UserSpaceInfo> getUserSpaceInfo(ToolContext context) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.getUserSpaceInfo(userId);
    }

    @Tool(name = "workspace_space_create", description = "为用户创建工作空间目录结构")
    public ApiResponse<UserSpaceInfo> createUserSpace(ToolContext context) {
        String userId = getUserId(context).orElse(MAIN_USER_ID);
        return client.createUserSpace(userId);
    }
}
