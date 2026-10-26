package com.symphonyai.catalog.exception;

/** Thrown when the action clashes with existing data (duplicate genre ...) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
