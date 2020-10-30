package com.simpledata.frame.base.exceptions;

import org.springframework.http.HttpStatus;

public class ExtendsException extends ApiException{
    public ExtendsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
