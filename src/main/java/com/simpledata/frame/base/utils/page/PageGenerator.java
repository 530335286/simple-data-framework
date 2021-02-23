package com.simpledata.frame.base.utils.page;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.simpledata.frame.base.entity.qo.PageQO;
import com.simpledata.frame.base.utils.Builder;

import java.util.List;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/2/23 14:44
 */
public class PageGenerator {

    public static <T> PageInfo<T> of(PageQO pageWrapper, QueryFunction<T> queryFunction) {
        pageWrapper = generateWrapper(pageWrapper);
        PageHelper.startPage(pageWrapper.getCurrent().intValue(), pageWrapper.getPageSize().intValue(), pageWrapper.getQueryNum());
        List<T> result = queryFunction.get();
        return PageInfo.of(result);
    }

    private static PageQO generateWrapper(PageQO pageWrapper) {
        if (pageWrapper == null) {
            pageWrapper = Builder.of(PageQO::new)
                    .with(PageQO::setCurrent, 1L)
                    .with(PageQO::setPageSize, 10L)
                    .with(PageQO::setQueryNum, Boolean.TRUE)
                    .build();
            return pageWrapper;
        }
        if (pageWrapper.getCurrent() == null) {
            pageWrapper.setCurrent(1L);
        }
        if (pageWrapper.getPageSize() == null) {
            pageWrapper.setPageSize(10L);
        }
        if (pageWrapper.getQueryNum() == null) {
            pageWrapper.setQueryNum(true);
        }
        return pageWrapper;
    }

}
