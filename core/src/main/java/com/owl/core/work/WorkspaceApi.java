package com.owl.core.work;

import com.owl.core.work.request.CreateUserScriptRequest;
import com.owl.core.work.request.CreateUserSpaceRequest;
import com.owl.core.work.request.ExecuteUserScriptRequest;
import com.owl.core.work.response.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface WorkspaceApi {

    @GetExchange("/health")
    HealthCheckResponse healthCheck();

    @PostExchange("/api/key")
    ApiResponse<String> generateApiKey();

    @PostExchange("/execute-user")
    ApiResponse<ScriptResult> executeUserScript(@RequestBody ExecuteUserScriptRequest request);

    @GetExchange("/scripts-user")
    ApiResponse<List<UserScriptInfo>> listUserScripts(@RequestParam("userId") String userId);

    @PostExchange("/scripts-user")
    ApiResponse<UserScriptInfo> createUserScript(@RequestBody CreateUserScriptRequest request);

    @PutExchange("/scripts-user")
    ApiResponse<UserScriptInfo> updateUserScript(@RequestBody UpdateUserScriptRequest request);

    @DeleteExchange("/scripts-user")
    ApiResponse<Map<String, String>> deleteUserScript(@RequestParam("userId") String userId, @RequestParam("name") String name);

    @GetExchange("/scripts-user/content")
    ApiResponse<ScriptContentResponse> getUserScriptContent(@RequestParam("userId") String userId, @RequestParam("name") String name);

    @GetExchange("/user-space")
    ApiResponse<UserSpaceInfo> getUserSpaceInfo(@RequestParam("userId") String userId);

    @PostExchange("/user-space")
    ApiResponse<UserSpaceInfo> createUserSpace(@RequestBody CreateUserSpaceRequest request);

    @PostExchange("/mcp")
    Map<String, Object> mcpRpc(@RequestBody Map<String, Object> body);
}