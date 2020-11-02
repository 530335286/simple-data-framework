package com.simpledata.frame.base.utils;

import com.simpledata.frame.base.annotations.EnableCache;
import com.simpledata.frame.base.annotations.EnableSimpleData;
import com.simpledata.frame.base.exceptions.derive.LoopException;
import com.simpledata.frame.base.service.BaseService;
import com.simpledata.frame.config.Init;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class ClassUtil {

    public static boolean loop(File folder, String packageName) {
        File[] files = folder.listFiles();
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            File file = files[fileIndex];
            if (file.isDirectory()) {
                loop(file, packageName + file.getName() + ".");
            } else {
                Map<String, Boolean> result = listMethodNames(file.getName(), packageName);
                if (result.get("continue")) {
                    continue;
                }
                throw new LoopException(result.get("init"));
            }
        }
        return false;
    }

    public static Map<String, Boolean> listMethodNames(String filename, String packageName) {
        Map<String, Boolean> result = new HashMap();
        try {
            String name = filename.substring(0, filename.length() - 5);
            Class<?> aClass = Class.forName(packageName + name);
            if (aClass.isAnnotationPresent(EnableSimpleData.class)) {
                Init.version = aClass.getAnnotation(EnableSimpleData.class).version();
                Init.mainClassName = aClass.getName();
                if (aClass.isAnnotationPresent(EnableCache.class)) {
                    Init.cacheTime = aClass.getAnnotation(EnableCache.class).value();
                    BaseService.isCache = true;
                } else {
                    Init.cacheTime = null;
                    BaseService.isCache = false;
                }
                result.put("continue", false);
                result.put("init", aClass.getAnnotation(EnableSimpleData.class).initClass());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put("continue", true);
        result.put("init", false);
        return result;
    }
}
