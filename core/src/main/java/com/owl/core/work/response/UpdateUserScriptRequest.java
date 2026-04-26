package com.owl.core.work.response;

import lombok.Data;

@Data
public class UpdateUserScriptRequest {
    private String userId;
    private String name;
    private String content;
}