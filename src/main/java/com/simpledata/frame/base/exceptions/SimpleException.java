package com.simpledata.frame.base.exceptions;

import org.springframework.http.HttpStatus;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class SimpleException extends RuntimeException {
    private HttpStatus code;

    protected SimpleException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }



    public HttpStatus getCode() {
        return this.code;
    }
}
