package com.tccb.virtualband.tool;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private String message;
    private T data;

    // 成功静态方法
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.data = data;
        r.message = "成功";
        return r;
    }

    // 失败静态方法
    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}