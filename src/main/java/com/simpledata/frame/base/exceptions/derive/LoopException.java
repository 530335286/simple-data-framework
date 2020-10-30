package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class LoopException extends ApiException {
    public LoopException(String message) {
        super(HttpStatus.OK, message);
    }
}
