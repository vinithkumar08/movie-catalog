package com.symphonyai.catalog.exception;

/** Thrown when something with the given id does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
