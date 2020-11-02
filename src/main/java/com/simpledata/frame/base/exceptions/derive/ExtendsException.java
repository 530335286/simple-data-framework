package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import org.springframework.http.HttpStatus;

public class ExtendsException extends SimpleException {
    public ExtendsException() {
        super(HttpStatus.BAD_REQUEST, "SimpleData : 实体未继承BaseEntity");
    }
}
