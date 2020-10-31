package com.simpledata.frame.base.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Data
public class ApiException extends RuntimeException {
    private HttpStatus code;

    protected ApiException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }



    public HttpStatus getCode() {
        return this.code;
    }
}