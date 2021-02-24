package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.values.Value;
import org.springframework.http.HttpStatus;

public class OkRequestException extends SimpleException {
    public OkRequestException() {
        super(HttpStatus.OK, Value.simple + "执行成功");
    }
}
