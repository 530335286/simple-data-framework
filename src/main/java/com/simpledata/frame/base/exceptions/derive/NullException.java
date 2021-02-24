package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.values.Value;
import org.springframework.http.HttpStatus;

public class NullException extends SimpleException {
    public NullException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NullException() {
        super(HttpStatus.NOT_FOUND, Value.simple + "查找不到此实体");
    }
}
