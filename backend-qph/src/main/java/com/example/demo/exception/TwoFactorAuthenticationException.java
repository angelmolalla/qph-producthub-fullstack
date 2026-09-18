package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TwoFactorAuthenticationException
        extends RuntimeException {

    public TwoFactorAuthenticationException(
            String message) {

        super(message);
    }
}