package com.simpledata.frame.base.enums;

public enum OrderEnum {

    Asc("asc"),
    Desc("desc");

    private String orderBy;

    private OrderEnum(String orderBy){
        this.orderBy=orderBy;
    }

    public String getOrderBy(){
        return this.orderBy;
    }
}
