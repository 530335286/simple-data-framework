package com.zcw.simpledata.base.enums;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public enum SqlEnum {
    Insert(1L),
    DeleteFalse(2L),
    DeleteTrue(3L),
    Update(4L),
    SelectPage(5L),
    SelectById(6L),
    Disable(7L),
    Enable(8L),
    Count(9L),
    BatchSave(10L);

    private Long sqlType;

    private SqlEnum(Long sqlType) {
        this.sqlType = sqlType;
    }

    public Long getSqlType() {
        return this.sqlType;
    }
}
