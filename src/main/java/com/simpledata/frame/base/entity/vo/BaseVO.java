package com.simpledata.frame.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.simpledata.frame.base.utils.TimeUtil;
import lombok.Data;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Data
public class BaseVO {
    @JsonIgnore
    private String createdAt = TimeUtil.Now();
    @JsonIgnore
    private String updatedAt = TimeUtil.Now();
    @JsonIgnore
    private Boolean enable = true;
    @JsonIgnore
    private Boolean deleted = false;
    @JsonIgnore
    private Long version = 0L;
}
