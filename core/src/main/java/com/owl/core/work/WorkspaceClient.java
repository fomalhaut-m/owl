package com.owl.core.work;

import com.owl.core.work.request.CreateUserScriptRequest;
import com.owl.core.work.request.CreateUserSpaceRequest;
import com.owl.core.work.request.ExecuteUserScriptRequest;
import com.owl.core.work.response.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;
import java.util.Map;

public class WorkspaceClient {

    private static final int DEFAULT_TIMEOUT = 30000;

    private final WorkspaceApi api;

    public WorkspaceClient(String baseUrl) {
        this(baseUrl, null);
    }

    public WorkspaceClient(String baseUrl, String apiKey) {
        String baseUri = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUri)
                .requestFactory(createRequestFactory(DEFAULT_TIMEOUT));

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }

        RestClient restClient = builder.build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(restClient)).build();

        this.api = factory.createClient(WorkspaceApi.class);
    }

    private SimpleClientHttpRequestFactory createRequestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(java.time.Duration.ofMillis(timeoutMs));
        return factory;
    }

    public WorkspaceApi api() {
        return api;
    }

    public HealthCheckResponse healthCheck() {
        return api.healthCheck();
    }

    public ApiResponse<ScriptResult> executeUserScript(String userId, String script, List<String> args, Integer timeout) {
        ExecuteUserScriptRequest request = new ExecuteUserScriptRequest();
        request.setUserId(userId);
        request.setScript(script);
        request.setArgs(args);
        request.setTimeout(timeout);
        return api.executeUserScript(request);
    }

    public ApiResponse<List<UserScriptInfo>> listUserScripts(String userId) {
        return api.listUserScripts(userId);
    }

    public ApiResponse<UserScriptInfo> createUserScript(String userId, String name, String content) {
        CreateUserScriptRequest request = new CreateUserScriptRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setContent(content);
        return api.createUserScript(request);
    }

    public ApiResponse<UserScriptInfo> updateUserScript(String userId, String name, String content) {
        UpdateUserScriptRequest request = new UpdateUserScriptRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setContent(content);
        return api.updateUserScript(request);
    }

    public ApiResponse<Map<String, String>> deleteUserScript(String userId, String name) {
        return api.deleteUserScript(userId, name);
    }

    public ApiResponse<ScriptContentResponse> getUserScriptContent(String userId, String name) {
        return api.getUserScriptContent(userId, name);
    }

    public ApiResponse<UserSpaceInfo> getUserSpaceInfo(String userId) {
        return api.getUserSpaceInfo(userId);
    }

    public ApiResponse<UserSpaceInfo> createUserSpace(String userId) {
        CreateUserSpaceRequest request = new CreateUserSpaceRequest();
        request.setUserId(userId);
        return api.createUserSpace(request);
    }

}