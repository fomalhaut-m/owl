package com.owl.core.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraceUtils 测试")
class TraceUtilsTest {

    @Test
    @DisplayName("获取 TraceId")
    void testGetTraceId() {
        String traceId = TraceUtils.getTraceId();
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
    }

    @Test
    @DisplayName("设置 TraceId")
    void testSetTraceId() {
        String customTraceId = "custom-trace-id-123";
        TraceUtils.setTraceId(customTraceId);
        assertEquals(customTraceId, TraceUtils.getTraceId());
        TraceUtils.clear();
    }

    @Test
    @DisplayName("生成 TraceId")
    void testGenerateTraceId() {
        String traceId = TraceUtils.generateTraceId();
        assertNotNull(traceId);
        assertEquals(traceId, TraceUtils.getTraceId());
        TraceUtils.clear();
    }

    @Test
    @DisplayName("清除 TraceId")
    void testClear() {
        TraceUtils.generateTraceId();
        TraceUtils.clear();
        assertNotEquals("custom", TraceUtils.getTraceId());
    }
}