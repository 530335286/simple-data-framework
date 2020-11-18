package com.simpledata.frame.base.entity.vo;

import lombok.Data;

import java.util.List;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

@Data
public class PageVO<T> {
    private Long current;
    private Long pageSize;
    private Boolean queryNum;
    private List<T> data;
    private Long total;
}
