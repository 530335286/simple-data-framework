package com.simpledata.frame.base.utils.page;

import java.util.List;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/28 15:25
 */
@FunctionalInterface
public interface QueryFunction<T> {

    List<T> get();

}
