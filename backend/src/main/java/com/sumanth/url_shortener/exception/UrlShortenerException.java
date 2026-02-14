package com.sumanth.url_shortener.exception;

import org.springframework.http.HttpStatus;

public abstract class UrlShortenerException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public UrlShortenerException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
