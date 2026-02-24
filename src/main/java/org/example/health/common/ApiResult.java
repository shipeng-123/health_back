package org.example.health.common;

import lombok.Data;

@Data
public class ApiResult<T> {
    private Integer code; // 0成功，1失败
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> ApiResult<T> fail(String msg) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(1);
        r.setMessage(msg);
        r.setData(null);
        return r;
    }
}