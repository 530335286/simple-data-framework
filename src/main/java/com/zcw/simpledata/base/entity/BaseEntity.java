package com.zcw.simpledata.base.entity;

import com.zcw.simpledata.base.utils.TimeUtil;
import lombok.Data;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Data
public class BaseEntity {
    private Long id;
    private String createdAt = TimeUtil.Now();
    private String updatedAt = TimeUtil.Now();
    private Boolean disabled = false;
    private Boolean deleted = false;
    private Long version = 0L;
}
