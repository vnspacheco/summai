package com.summai.api.dto;

public class ApiErrorResponse {

    private final String error;
    private final String code;

    public ApiErrorResponse(String error, String code) {
        this.error = error;
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }
}
