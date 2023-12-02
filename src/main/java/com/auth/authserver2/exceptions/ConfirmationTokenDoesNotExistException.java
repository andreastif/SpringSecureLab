package com.auth.authserver2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConfirmationTokenDoesNotExistException extends RuntimeException {
    public ConfirmationTokenDoesNotExistException(String message) {
        super(message);
    }
}
