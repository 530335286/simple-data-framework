package com.zcw.simpledata.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zcw.simpledata.config.FrameConfig;
import org.springframework.context.annotation.Import;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({FrameConfig.class})
public @interface EnableSimpleData {
    /**
     * 是否生成基础类
     * @return
     */
    boolean initClass() default false;
}
