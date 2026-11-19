package com.nhom9.smartmall.common.exception;

import lombok.Getter;

/** Ngoại lệ nghiệp vụ duy nhất được ném từ tầng service. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static BusinessException notFound(String what) {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy " + what);
    }
}
