package com.hiretrack.exception;

// Thrown when something doesn't exist (or doesn't belong to the current user). Becomes HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
