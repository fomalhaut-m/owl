package com.owl.core.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OwlException 测试")
class OwlExceptionTest {

    @Test
    @DisplayName("默认错误码")
    void testDefaultCode() {
        var exception = new OwlException("test message");
        assertEquals("test message", exception.getMessage());
        assertEquals("OWL_ERROR", exception.getCode());
    }

    @Test
    @DisplayName("自定义错误码")
    void testCustomCode() {
        var exception = new OwlException("CUSTOM_CODE", "custom message");
        assertEquals("custom message", exception.getMessage());
        assertEquals("CUSTOM_CODE", exception.getCode());
    }

    @Test
    @DisplayName("带 cause 的异常")
    void testWithCause() {
        var cause = new RuntimeException("original cause");
        var exception = new OwlException("ERROR_CODE", "message with cause", cause);
        assertEquals("message with cause", exception.getMessage());
        assertEquals("ERROR_CODE", exception.getCode());
        assertEquals(cause, exception.getCause());
    }
}