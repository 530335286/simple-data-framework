package com.simpledata.frame.base.entity;

import lombok.Data;

import java.util.List;

@Data
public class CacheData<T> {
    private Long time;
    private List<T> data;
}
