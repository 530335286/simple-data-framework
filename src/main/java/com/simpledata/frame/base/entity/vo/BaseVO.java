package com.simpledata.frame.base.entity.vo;

import com.simpledata.frame.base.utils.TimeUtil;
import lombok.Data;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Data
public class BaseVO {
    private String createdAt = TimeUtil.Now();
    private String updatedAt = TimeUtil.Now();
    private Boolean disabled = false;
    private Boolean deleted = false;
    private Long version = 0L;
}
