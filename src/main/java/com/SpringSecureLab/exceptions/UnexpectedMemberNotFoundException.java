package com.SpringSecureLab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedMemberNotFoundException extends RuntimeException {
    public UnexpectedMemberNotFoundException(String message) {
        super(message);
    }
}
