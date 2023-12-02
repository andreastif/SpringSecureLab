package com.auth.authserver2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedConfirmationTokenUpdateException extends RuntimeException{
    public UnexpectedConfirmationTokenUpdateException(String message) {
        super(message);
    }
}
