package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.values.Value;
import org.springframework.http.HttpStatus;

public class IdException extends SimpleException {
    public IdException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public IdException() {
        super(HttpStatus.BAD_REQUEST, Value.simple + "id不能为空");
    }
}
