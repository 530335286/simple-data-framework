package com.simpledata.frame.base.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ExceptionAspect {

    @Around(value = "execution(* com.simpledata.frame.base.controller.*.*(..))")
    public Object handleThrowing(ProceedingJoinPoint pjp) {
        Map<String,Object> map = new HashMap();
        try {
            Object o=pjp.proceed();
            return o;
        }catch (DataIntegrityViolationException | SQLIntegrityConstraintViolationException e){
            e.printStackTrace();
            map.put("code","500");
            map.put("message","sql执行有误 请查看控制台");
            return ResponseEntity.ok(map);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            map.put("code","500");
            map.put("message","程序异常 请查看控制台");
            return ResponseEntity.ok(map);
        }
    }
}
