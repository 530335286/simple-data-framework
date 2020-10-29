package com.zcw.simpledata.base.utils;

import com.zcw.simpledata.base.annotations.Id;
import com.zcw.simpledata.base.entity.BaseEntity;
import com.zcw.simpledata.base.entity.qo.PageQO;
import com.zcw.simpledata.base.enums.SqlEnum;
import com.zcw.simpledata.base.exceptions.ExtendsException;
import com.zcw.simpledata.base.exceptions.NullException;
import com.zcw.simpledata.base.mapper.ClassMapper;
import com.zcw.simpledata.config.Init;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Log4j2
public class SqlUtil<T, D> {

    public Class entityClass;

    public Class voClass;

    public String tableName;

    public String idName;

    public String id;

    public ClassMapper<T, D> classMapper;

    public boolean isExtends;

    private SqlUtil() {

    }

    @SneakyThrows
    public SqlUtil(Class entity, Class vo) {
        this.entityClass = entity;
        this.voClass = vo;
        this.tableName = SqlUtil.humpToUnderline(this.entityClass.getSimpleName()).toLowerCase();
        this.classMapper = new ClassMapper(this.entityClass, this.voClass);
        this.isExtends = (T) entityClass.newInstance() instanceof BaseEntity;
        for (Field field : entity.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                this.id = field.getName();
                this.idName = SqlUtil.humpToUnderline(this.id);
            }
        }
    }

    public Field[] concat(Field[] first, Field[] second) {
        Field[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String humpToUnderline(String para) {
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;
        if (!para.contains("_")) {
            for (int i = 1; i < para.length(); ++i) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, "_");
                    ++temp;
                }
            }
        }
        return sb.toString().toUpperCase();
    }

    public static String getConvert(String str) {
        String first = str.substring(0, 1);
        String after = str.substring(1);
        first = first.toUpperCase();
        return first + after;
    }

    @SneakyThrows
    private String[] generateField(List<T> valueList) {
        Field[] fields = this.entityClass.getDeclaredFields();
        if (isExtends) {
            fields = this.concat(fields, BaseEntity.class.getDeclaredFields());
        }
        String fieldLine = "";
        String valueLine = "values";
        String[] fieldAndValue = new String[2];
        for (int j = 0; j < valueList.size(); j++) {
            T value = valueList.get(j);
            valueLine += "(";
            for (int i = 0; i < fields.length; ++i) {
                Field field = fields[i];
                String fieldName = field.getName();
                if (!fieldName.equalsIgnoreCase(idName)) {
                    String methodName = "get" + getConvert(fieldName);
                    Method method = this.entityClass.getMethod(methodName, null);
                    Object obj = method.invoke(value, null);
                    if (j == 0) {
                        fieldName = this.humpToUnderline(fieldName);
                        fieldLine = fieldLine + fieldName + ",";
                    }
                    if (field.getType() == Boolean.class) {
                        valueLine = valueLine + obj + ",";
                    } else {
                        valueLine = valueLine + "'" + obj + "',";
                    }
                }
            }
            valueLine = valueLine.substring(0, valueLine.length() - 1);
            valueLine += "),";
        }
        fieldLine = fieldLine.substring(0, fieldLine.length() - 1);
        valueLine = valueLine.substring(0, valueLine.length() - 1);
        fieldAndValue[0] = fieldLine;
        fieldAndValue[1] = valueLine;
        return fieldAndValue;
    }

    @SneakyThrows
    private String forUpdateSql(T value) {
        Field[] fields = this.entityClass.getDeclaredFields();
        if (isExtends) {
            fields = this.concat(fields, BaseEntity.class.getDeclaredFields());
        }
        String sql = "";
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            if (!fieldName.equalsIgnoreCase(idName)) {
                Method method = this.entityClass.getMethod("get" + getConvert(fieldName));
                Object obj = method.invoke(value, (Object[]) null);
                if (null != obj) {
                    fieldName = this.humpToUnderline(fieldName);
                    if (field.getType() == Boolean.class) {
                        sql = sql + fieldName + "=" + obj + ",";
                    } else {
                        sql = sql + fieldName + "='" + obj + "',";
                    }
                }
            }
        }
        sql = sql.substring(0, sql.length() - 1);
        return sql;
    }

    @SneakyThrows
    private String updateVersion(String sql, Long id, T entity) {
        if (null == entity) {
            throw new NullException("查找不到此实体");
        }
        Method method = entityClass.getMethod("getVersion", null);
        Long version = (Long) method.invoke(entity, null);
        sql += " and version = " + version;
        return sql;
    }

    private String isDelete(String sql) {
        if (isExtends) {
            sql += " and deleted = false";
        }
        return sql;
    }

    public String generateSql(SqlEnum sqlEnum, List<T> value, Long id, PageQO pageQO) {
        String sql = "";
        switch (sqlEnum.getSqlType().intValue()) {
            case 1:
                String[] fieldAndValue = this.generateField(value);
                sql = "insert into " + this.tableName + "(" + fieldAndValue[0] + ") " + fieldAndValue[1];
                break;
            case 2:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set deleted = true , version = version + 1 where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException("乐观锁只支持BaseEntity类型");
                    }
                } else {
                    sql = "update " + this.tableName + " set deleted = true where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case 3:
                sql = "delete from " + this.tableName + " where " + idName + " = " + id;
                sql = isDelete(sql);
                break;
            case 4:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set " + this.forUpdateSql(value.get(0)) + ", version = version +1 where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException("乐观锁只支持BaseEntity类型");
                    }
                } else {
                    sql = "update " + this.tableName + " set " + this.forUpdateSql(value.get(0)) + " where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case 5:
                Long begin = (pageQO.getCurrent() - 1L) * pageQO.getPageSize();
                sql = "select * from " + this.tableName;
                if (isExtends) {
                    sql += " where deleted = false";
                }
                sql += " limit " + begin + "," + pageQO.getPageSize();
                break;
            case 6:
                sql = "select * from " + this.tableName + " where " + idName + " = " + id;
                sql = isDelete(sql);
                break;
            case 7:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set disabled = true,version = version + 1 where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException("乐观锁只支持BaseEntity类型");
                    }
                } else {
                    sql = "update " + this.tableName + " set disabled = true where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case 8:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set disabled = false,version = version + 1 where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException("乐观锁只支持BaseEntity类型");
                    }
                } else {
                    sql = "update " + this.tableName + " set disabled = false where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case 9:
                sql = "select count(1) from " + this.tableName;
                if (isExtends) {
                    sql += " where deleted = false";
                }
                break;
            case 10:
                fieldAndValue = this.generateField(value);
                sql = "insert into " + this.tableName + "(" + fieldAndValue[0] + ") " + fieldAndValue[1];
                break;
        }
        log.info("执行的SQL:" + sql);
        return sql;
    }
}
