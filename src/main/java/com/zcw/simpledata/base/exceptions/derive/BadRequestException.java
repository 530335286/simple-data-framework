package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Simple-Data : 执行失败");
    }

    public BadRequestException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
