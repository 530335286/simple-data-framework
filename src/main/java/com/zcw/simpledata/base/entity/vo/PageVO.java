package com.zcw.simpledata.base.entity.vo;

import lombok.Data;

import java.util.List;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Data
public class PageVO<T> {
    private Long current;
    private Long pageSize;
    private Boolean queryNum;
    private List<T> data;
    private Long num;
}
