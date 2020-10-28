package com.zcw.simpledata.base.handler;

import com.zcw.simpledata.base.exceptions.ApiException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@ControllerAdvice
@Log4j2
public abstract class ExceptionsHandler {

    @ResponseBody
    @ExceptionHandler({Throwable.class})
    public abstract ResponseEntity doException(Throwable throwable);

    @ResponseBody
    @ExceptionHandler({ApiException.class})
    public abstract ResponseEntity doApiException(ApiException apiException);
}
