package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class LoopException extends ApiException {
    public LoopException(String message) {
        super(HttpStatus.OK, message);
    }
}
