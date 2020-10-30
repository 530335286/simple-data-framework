package com.simpledata.frame.base.entity.qo;

import com.simpledata.frame.base.enums.OrderEnum;

import java.util.Map;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class PageQO {
    private Long current;
    private Long pageSize;
    private Boolean queryNum;

    public Long getCurrent() {
        return null == this.current ? 1L : this.current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getPageSize() {
        return null == this.pageSize ? 10L : this.pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getQueryNum() {
        return null == this.queryNum ? true : this.queryNum;
    }

    public void setQueryNum(Boolean queryNum) {
        this.queryNum = queryNum;
    }
}
