package com.owl.core.common;

import java.util.UUID;

public class TraceUtils {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString();
        TRACE_ID.set(traceId);
        return traceId;
    }
}