package com.wedservice.backend.common.controller;

import com.wedservice.backend.common.response.ApiResponse;

public abstract class BaseController {

    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    protected <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message);
    }
}
