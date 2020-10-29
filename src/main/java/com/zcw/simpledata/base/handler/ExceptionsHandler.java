package com.zcw.simpledata.base.handler;

import com.zcw.simpledata.base.exceptions.ApiException;
import com.zcw.simpledata.base.exceptions.derive.BadRequestException;
import com.zcw.simpledata.base.exceptions.derive.OkRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@RestControllerAdvice
@Log4j2
public abstract class ExceptionsHandler {

    @ResponseBody
    @ExceptionHandler({OkRequestException.class})
    public ResponseEntity ok(OkRequestException okRequestException){
        return ResponseEntity.ok("Simple-Data : 执行成功");
    }

    @ResponseBody
    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity bad(BadRequestException badRequestException){
        return ResponseEntity.ok("Simple-Data : 执行失败");
    }

    @ResponseBody
    @ExceptionHandler({Throwable.class})
    public abstract ResponseEntity doException(Throwable throwable);

    @ResponseBody
    @ExceptionHandler({ApiException.class})
    public abstract ResponseEntity doApiException(ApiException apiException);
}
