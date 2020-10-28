package com.zcw.simpledata.base.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class TimeUtil {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String Now() {
        return simpleDateFormat.format(new Date());
    }
}
