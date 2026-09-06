package com.leconsulat.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base unchecked exception carrying an HTTP status and an error code, consumed by
 * {@link com.leconsulat.common.web.GlobalExceptionHandler} to build the uniform error
 * response shape defined in the API contract.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
