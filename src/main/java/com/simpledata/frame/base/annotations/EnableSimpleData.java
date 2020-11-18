package com.simpledata.frame.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simpledata.frame.config.FrameConfig;
import org.springframework.context.annotation.Import;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
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

    /**
     * 是否开启乐观锁支持
     * @return
     */
    boolean version() default false;
}
