package com.zcw.simpledata.config;

public class SqlTable {
    private String fieldName;
    private String fieldType;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        if (fieldType.contains("(")) {
            fieldType = fieldType.substring(0, fieldType.indexOf("("));
        }
        switch (fieldType) {
            case "bit":
                fieldType = "boolean";
                break;
            case "id":
                fieldType = "long";
                break;
            default:
                fieldType = "String";
                break;
        }
        if (this.fieldName.equals("id")) {
            fieldType = "long";
        }
        this.fieldType = fieldType;
    }
}
