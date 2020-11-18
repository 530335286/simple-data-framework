package com.simpledata.frame.base.annotations;

import java.lang.annotation.*;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Id {
}
