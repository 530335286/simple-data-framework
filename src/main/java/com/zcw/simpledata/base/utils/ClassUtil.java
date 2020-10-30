package com.zcw.simpledata.base.utils;

import com.zcw.simpledata.base.annotations.EnableCache;
import com.zcw.simpledata.base.annotations.EnableSimpleData;
import com.zcw.simpledata.base.exceptions.derive.LoopException;
import com.zcw.simpledata.config.Init;

import java.io.File;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class ClassUtil {

    public static boolean loop(File folder, String packageName){
        File[] files = folder.listFiles();
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            File file = files[fileIndex];
            if (file.isDirectory()) {
                loop(file, packageName + file.getName() + ".");
            } else {
                boolean init = listMethodNames(file.getName(), packageName);
                if (init) {
                    throw new LoopException(null);
                }
            }
        }
        return false;
    }

    public static boolean listMethodNames(String filename, String packageName) {
        try {
            String name = filename.substring(0, filename.length() - 5);
            Class<?> aClass = Class.forName(packageName + name);
            if (aClass.isAnnotationPresent(EnableSimpleData.class)) {
                Init.version = aClass.getAnnotation(EnableSimpleData.class).version();
                Init.mainClassName = aClass.getName();
                if (aClass.isAnnotationPresent(EnableCache.class)) {
                    Init.cacheTime = aClass.getAnnotation(EnableCache.class).value();
                } else {
                    Init.cacheTime = null;
                }
                return aClass.getAnnotation(EnableSimpleData.class).initClass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
