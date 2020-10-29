package com.zcw.simpledata.base.entity;

import lombok.Data;

import java.util.List;

@Data
public class CacheData<T> {
    private Long time;
    private List<T> data;
}
