package com.owl.core.work.response;

import lombok.Data;

@Data
public class UserScriptInfo {
    private String name;
    private String path;
    private long size;
    private long createdTime;
    private long modifiedTime;
}