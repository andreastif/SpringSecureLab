package com.SpringSecureLab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomPasswordValidationException extends RuntimeException{
    public CustomPasswordValidationException(String message) {
        super(message);
    }
}
