package com.zcw.simpledata.base.exceptions;

import org.springframework.http.HttpStatus;

public class LoopException extends ApiException{
    public LoopException(String message) {
        super(HttpStatus.OK, message);
    }
}
