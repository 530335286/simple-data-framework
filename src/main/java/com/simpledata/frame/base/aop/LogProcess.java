package com.simpledata.frame.base.aop;

import com.simpledata.frame.base.annotations.Log;
import com.simpledata.frame.base.utils.TimeUtil;
import com.simpledata.frame.config.Init;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component
@Aspect
@Log4j2
public class LogProcess {

    @Around(value = "@annotation(com.simpledata.frame.base.annotations.Log)")
    public Object doLog(ProceedingJoinPoint joinPoint) {
        Object result = null;
        String methodName = null;
        Object[] args = null;
        Object[] params = null;
        String now = null;
        if (Init.isLog) {
            now = TimeUtil.Now();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            Parameter[] parameters = method.getParameters();
            Class<?> controller = joinPoint.getTarget().getClass();
            methodName = (controller.isAnnotationPresent(Log.class) ? controller.getAnnotation(Log.class).value() : controller.getName()) + " : " +
                    (StringUtils.isEmpty(method.getAnnotation(Log.class).value()) ? method.getName() : method.getAnnotation(Log.class).value());
            args = joinPoint.getArgs();
            params = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                String paramName = parameters[i].getName();
                Object param = args[i];
                if (param == null) {
                    params[i] = paramName + ":" + null;
                    continue;
                }
                JSONObject jsonParam = new JSONObject(param);
                String paramValue = paramName + ":" + jsonParam.toString();
                params[i] = paramValue;
            }
        }
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (Init.isLog) {
                log.error("Simple-Data : " + now + " 接口: " + methodName + " 参数: " + (args != null ? JSONObject.valueToString(params) : null) + " 异常信息: " + throwable.getMessage());
            }
        }
        if (Init.isLog) {
            log.info("Simple-Data : " + now + " 接口: " + methodName + " 参数: " + (args != null ? JSONObject.valueToString(params) : null) + " 返回值: " + (result != null ? JSONObject.valueToString(result) : null
            ));
        }
        return result;
    }
}
