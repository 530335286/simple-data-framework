package com.simpledata.frame.base.handler;

import com.simpledata.frame.base.exceptions.SimpleException;
import com.simpledata.frame.base.exceptions.derive.BadRequestException;
import com.simpledata.frame.base.exceptions.derive.OkRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

@ControllerAdvice(basePackages = "com.simpledata.frame.controller")
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

    /**
     * 全局异常拦截
     * @param throwable
     * @return
     */
    @ResponseBody
    @ExceptionHandler({Throwable.class})
    public abstract Object doException(Throwable throwable);

    /**
     * 自定义异常拦截
     * @param simpleException
     * @return
     */
    @ResponseBody
    @ExceptionHandler({SimpleException.class})
    public abstract Object doApiException(SimpleException simpleException);
}
