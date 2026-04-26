package com.owl.core.work.response;

import lombok.Data;

@Data
public class ScriptResult {
    private Integer returncode;
    private String stdout;
    private String stderr;
}