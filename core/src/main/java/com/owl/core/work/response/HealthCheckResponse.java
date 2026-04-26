package com.owl.core.work.response;

import lombok.Data;

@Data
public class HealthCheckResponse {
    private String status;
    private boolean authenticated;
    private String pythonVersion;
}