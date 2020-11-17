package com.simpledata.frame.config;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

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
                fieldType = "Boolean";
                break;
            case "int":
            case "bigint":
                fieldType = "Long";
                break;
            default:
                fieldType = "String";
                break;
        }
        if (this.fieldName.equals("id")) {
            fieldType = "Long";
        }
        this.fieldType = fieldType;
    }
}
