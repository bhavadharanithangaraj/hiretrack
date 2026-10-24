package com.hiretrack.exception;

// Thrown when registering with an email that is already taken. Becomes HTTP 409 Conflict.
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email " + email + " already exists");
    }
}
