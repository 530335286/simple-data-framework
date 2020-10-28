package com.zcw.simpledata.base.exceptions;

import org.springframework.http.HttpStatus;

public class NullException extends ApiException{
    public NullException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
