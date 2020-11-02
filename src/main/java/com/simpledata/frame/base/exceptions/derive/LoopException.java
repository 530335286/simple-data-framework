package com.simpledata.frame.base.exceptions.derive;

import com.simpledata.frame.base.exceptions.SimpleException;
import org.springframework.http.HttpStatus;

public class LoopException extends SimpleException {

    private Boolean init;

    public LoopException(String message) {
        super(HttpStatus.OK, message);
    }

    public LoopException(Boolean init) {
        super(HttpStatus.OK, null);
        this.init = init;
    }

    public Boolean getInit() {
        return this.init;
    }
}
