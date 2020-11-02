package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import org.springframework.http.HttpStatus;

public class OkRequestException extends SimpleException {
    public OkRequestException() {
        super(HttpStatus.OK, "Simple-Data : 执行成功");
    }
}
