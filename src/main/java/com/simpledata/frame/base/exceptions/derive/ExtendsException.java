package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.values.Value;
import org.springframework.http.HttpStatus;

public class ExtendsException extends SimpleException {
    public ExtendsException() {
        super(HttpStatus.BAD_REQUEST, Value.simple + "实体未继承BaseEntity");
    }
}
