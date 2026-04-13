package com.owl.core.common.exception;

public class OwlException extends RuntimeException {
    private final String code;

    public OwlException(String message) {
        super(message);
        this.code = "OWL_ERROR";
    }

    public OwlException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OwlException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}