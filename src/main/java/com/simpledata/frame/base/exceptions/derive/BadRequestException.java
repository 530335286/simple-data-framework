package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.values.Value;
import org.springframework.http.HttpStatus;

public class BadRequestException extends SimpleException {

    public BadRequestException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, Value.simple + "执行失败");
    }

    public BadRequestException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
