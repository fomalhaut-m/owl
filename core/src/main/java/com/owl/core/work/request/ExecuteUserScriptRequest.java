package com.owl.core.work.request;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteUserScriptRequest {
    private String userId;
    private String script;
    private List<String> args;
    private Integer timeout;
}