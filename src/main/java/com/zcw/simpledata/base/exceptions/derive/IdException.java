package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class IdException extends ApiException {
    public IdException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public IdException() {
        super(HttpStatus.BAD_REQUEST, "Simple-Data : id不能为空");
    }
}
