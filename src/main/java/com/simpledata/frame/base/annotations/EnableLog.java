package com.simpledata.frame.base.annotations;

import java.lang.annotation.*;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableLog {
}
