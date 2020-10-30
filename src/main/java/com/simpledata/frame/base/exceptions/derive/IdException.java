package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class IdException extends ApiException {
    public IdException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public IdException() {
        super(HttpStatus.BAD_REQUEST, "Simple-Data : id不能为空");
    }
}
