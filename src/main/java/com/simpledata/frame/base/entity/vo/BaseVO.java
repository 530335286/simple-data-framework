package com.simpledata.frame.base.entity.vo;

import com.simpledata.frame.base.utils.TimeUtil;
import lombok.Data;

/***
 * simple-data1.0
 * @author zcw && Jiuchen
 * @version 1.0
 */

@Data
public class BaseVO {
    private String createdAt = TimeUtil.Now();
    private String updatedAt = TimeUtil.Now();
    private Boolean disabled = false;
    private Boolean deleted = false;
    private Long version = 0L;
}
