package com.owl.core.work.request;

import lombok.Data;

@Data
public class CreateUserScriptRequest {
    private String userId;
    private String name;
    private String content;
}