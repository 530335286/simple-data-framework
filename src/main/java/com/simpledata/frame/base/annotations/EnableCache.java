package com.simpledata.frame.base.annotations;

import java.lang.annotation.*;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableCache {
    /**
     * 缓存有效时间(s)
     * @return
     */
    long value() default 600l;
}
