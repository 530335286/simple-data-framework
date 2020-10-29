package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class NullException extends ApiException {
    public NullException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NullException() {
        super(HttpStatus.NOT_FOUND, "Simple-Data : 查找不到此实体");
    }
}
