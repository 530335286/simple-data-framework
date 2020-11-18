package com.simpledata.frame.base.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

public class TimeUtil {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String Now() {
        return simpleDateFormat.format(new Date());
    }
}
