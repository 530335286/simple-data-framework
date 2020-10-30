package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ExtendsException extends ApiException {
    public ExtendsException() {
        super(HttpStatus.BAD_REQUEST, "SimpleData : 实体未继承BaseEntity");
    }
}
