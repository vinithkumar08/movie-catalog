package com.symphonyai.catalog.exception;

/** Thrown when the request breaks a business rule (e.g. too many genres) -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
