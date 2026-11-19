package com.nhom9.smartmall.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Định dạng lỗi thống nhất: { code, message, details? } (mục 4.1 thiết kế API). */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, List<FieldViolation> details) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }
}
