package com.owl.core.work.response;

import lombok.Data;

@Data
public class UserSpaceInfo {
    private String userId;
    private String workspacePath;
    private boolean exists;
    private long sizeBytes;
    private String sizeFormatted;
    private boolean created;
    private String message;
}