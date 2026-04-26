package com.owl.core.work.response;

import lombok.Data;

@Data
public class ScriptContentResponse {
    private String name;
    private String userId;
    private String content;
    private int size;
}