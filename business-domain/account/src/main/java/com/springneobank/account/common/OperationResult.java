package com.springneobank.account.common;

public class OperationResult<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> OperationResult<T> ok(T data) {
        OperationResult<T> r = new OperationResult<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> OperationResult<T> fail(String message) {
        OperationResult<T> r = new OperationResult<>();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}

