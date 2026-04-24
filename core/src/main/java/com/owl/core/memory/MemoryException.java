package com.owl.core.memory;

/**
 * 记忆仓库操作异常
 * <p>
 * 用于封装 FileMemoryRepo 操作过程中发生的各类 IO 异常，
 * 提供清晰的错误信息便于排查问题。
 * </p>
 *
 * @author Owl Team
 * @since 2026-04-23
 */
public class MemoryException extends RuntimeException {

    /**
     * 使用消息和原因构造异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public MemoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 仅使用消息构造异常
     *
     * @param message 错误消息
     */
    public MemoryException(String message) {
        super(message);
    }
}
