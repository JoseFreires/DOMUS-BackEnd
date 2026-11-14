package com.domus.tcc.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StatusInvalidoException extends RuntimeException {

    public StatusInvalidoException(String message) {
        super(message);
    }
}
