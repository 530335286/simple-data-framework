package com.zcw.simpledata.base.exceptions;

import org.springframework.http.HttpStatus;

public class ExtendsException extends ApiException{
    public ExtendsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
