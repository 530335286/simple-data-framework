package com.zcw.simpledata.base.utils;

import com.zcw.simpledata.base.annotations.EnableSimpleData;

import java.io.File;

public class ClassUtil {

    private static int initClass = 0;

    public static boolean loop(File folder, String packageName) throws Exception {
        File[] files = folder.listFiles();
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            File file = files[fileIndex];
            if (file.isDirectory()) {
                loop(file, packageName + file.getName() + ".");
            } else {
                boolean init = listMethodNames(file.getName(), packageName);
                if (init) {
                    initClass++;
                }
            }
        }
        return initClass > 0;
    }

    public static boolean listMethodNames(String filename, String packageName) {
        try {
            String name = filename.substring(0, filename.length() - 5);
            Class<?> aClass = Class.forName(packageName + name);
            if (aClass.isAnnotationPresent(EnableSimpleData.class)) {
                return aClass.getAnnotation(EnableSimpleData.class).initClass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
