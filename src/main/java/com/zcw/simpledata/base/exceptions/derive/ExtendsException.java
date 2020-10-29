package com.zcw.simpledata.base.exceptions.derive;

import com.zcw.simpledata.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ExtendsException extends ApiException {
    public ExtendsException() {
        super(HttpStatus.BAD_REQUEST, "SimpleData : 实体未继承BaseEntity");
    }
}
