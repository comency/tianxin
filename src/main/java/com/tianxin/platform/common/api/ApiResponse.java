package com.tianxin.platform.common.api;

import java.time.Instant;

/** Standard response envelope for public platform APIs. */
public record ApiResponse<T>(boolean success, String code, String message, T data, Instant timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "操作成功", data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null, Instant.now());
    }
}
