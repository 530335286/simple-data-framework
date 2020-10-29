package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class OkRequestException extends ApiException {
    public OkRequestException() {
        super(HttpStatus.OK, "Simple-Data : 执行成功");
    }
}
