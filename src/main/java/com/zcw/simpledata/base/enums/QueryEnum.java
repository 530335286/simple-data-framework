package com.zcw.simpledata.base.enums;

public enum QueryEnum {
    gt(" > "),
    ge(" >= "),
    eq(" = "),
    le(" <= "),
    lt(" < "),
    like(" like "),
    notEq(" != "),
    notLike(" not like ");

    private String operator;

    private QueryEnum(String operator) {
        this.operator = operator;
    }
    public String getOperator(){
        return this.operator;
    }
}