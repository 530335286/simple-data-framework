package com.simpledata.frame.base.utils;

import com.simpledata.frame.base.annotations.Id;
import com.simpledata.frame.base.entity.BaseEntity;
import com.simpledata.frame.base.entity.qo.PageQO;
import com.simpledata.frame.base.enums.OrderEnum;
import com.simpledata.frame.base.enums.QueryEnum;
import com.simpledata.frame.base.enums.SqlEnum;
import com.simpledata.frame.base.exceptions.derive.ExtendsException;
import com.simpledata.frame.base.exceptions.derive.NullException;
import com.simpledata.frame.base.mapper.ClassMapper;
import com.simpledata.frame.base.service.BaseService;
import com.simpledata.frame.base.values.Value;
import com.simpledata.frame.config.Init;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Log4j2
public class SqlUtil<T, D> {

    public Class entityClass;

    private Class voClass;

    private String tableName;

    private String idName;

    public String id;

    public ClassMapper<T, D> classMapper;

    public boolean isExtends;

    private BaseService<T, D> service;

    private Field[] fields;

    private Method getVersion;

    private Method[] methods;

    private SqlUtil() {

    }

    @SneakyThrows
    public SqlUtil(Class entity, Class vo, BaseService service) {
        this.entityClass = entity;
        this.voClass = vo;
        this.service = service;
        this.tableName = SqlUtil.humpToUnderline(this.entityClass.getSimpleName()).toLowerCase();
        this.classMapper = new ClassMapper(this.entityClass, this.voClass);
        this.isExtends = (T) entityClass.newInstance() instanceof BaseEntity;
        this.fields = entityClass.getDeclaredFields();
        if (isExtends) {
            this.fields = this.concat(fields, BaseEntity.class.getDeclaredFields());
            this.getVersion = entityClass.getMethod(Value.getVersion, null);
        }
        this.methods = new Method[this.fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            this.methods[i] = entityClass.getMethod(Value.get + getConvert(fieldName), null);
            if (field.isAnnotationPresent(Id.class)) {
                this.id = fieldName;
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
        if (!para.contains(Value.underLine)) {
            for (int i = 1; i < para.length(); ++i) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, Value.underLine);
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
                    Method method = methods[i];
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
        String sql = "";
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            if (!fieldName.equalsIgnoreCase(idName)) {
                Method method = methods[i];
                Object obj = method.invoke(value, null);
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
            throw new NullException();
        }
        entity = classMapper.voTOEntity(service.queryById(id).getBody());
        Long version = (Long) getVersion.invoke(entity, null);
        sql += " and version = " + version;
        return sql;
    }

    private String isDelete(String sql) {
        if (isExtends) {
            sql += " and deleted = false";
        }
        return sql;
    }

    private String appendCondition(String sql, T value, Map<String, QueryEnum> condition, String append) {
        if (value == null) {
            throw new NullPointerException();
        }
        Field[] fields = entityClass.getDeclaredFields();
        int findFieldNum = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = humpToUnderline(field.getName());
            QueryEnum queryEnum = condition.get(field.getName());
            if (queryEnum == null) {
                continue;
            }
            String operator = queryEnum.getOperator();
            String upName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            Method method = null;
            try {
                method = entityClass.getMethod(Value.get + upName, null);
            } catch (NoSuchMethodException e) {
                continue;
            }
            Object fieldValue = null;
            try {
                fieldValue = method.invoke(value, null);
                if (fieldValue == null) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (findFieldNum == 0) {
                sql += append;
            } else {
                sql += " and ";
            }
            findFieldNum++;
            sql = sql + fieldName + operator;
            if (queryEnum == QueryEnum.like || queryEnum == QueryEnum.notLike) {
                sql += "'%" + fieldValue + "%'";
            } else {
                sql += fieldValue;
            }
        }
        return sql;
    }

    private String orderBy(String sql, Map<String, OrderEnum> orderEnumMap) {
        Set<Map.Entry<String, OrderEnum>> set = orderEnumMap.entrySet();
        Iterator<Map.Entry<String, OrderEnum>> iterator = set.iterator();
        int orderNum = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, OrderEnum> entry = iterator.next();
            String fieldName = entry.getKey();
            fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                entityClass.getMethod(Value.get + fieldName, null);
                String underName = humpToUnderline(entry.getKey());
                OrderEnum orderEnum = entry.getValue();
                if (orderEnum == null) {
                    continue;
                }
                String order = orderEnum.getOrderBy();
                if (orderNum == 0) {
                    sql += " order by ";
                }
                sql = sql + underName + " " + order;
                if (iterator.hasNext()) {
                    sql += ",";
                }
                orderNum++;
            } catch (NoSuchMethodException e) {
                continue;
            }
        }
        if (sql.substring(sql.length() - 1).equals(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

    public String generateSql(SqlEnum sqlEnum, List<T> value, Long id, PageQO pageQO, Map<String, QueryEnum> condition, Map<String, OrderEnum> orderEnumMap) {
        String sql = "";
        switch (sqlEnum) {
            case Insert:
                String[] fieldAndValue = this.generateField(value);
                sql = "insert into " + this.tableName + "(" + fieldAndValue[0] + ") " + fieldAndValue[1];
                break;
            case DeleteFalse:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set deleted = true , version = version + 1 , updated_at = \'" + TimeUtil.Now() + "\' where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException();
                    }
                } else {
                    sql = "update " + this.tableName + " set deleted = true where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case DeleteTrue:
                sql = "delete from " + this.tableName + " where " + idName + " = " + id;
                break;
            case Update:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set " + this.forUpdateSql(value.get(0)) + ", version = version +1 , updated_at = \'" + TimeUtil.Now() + "\' where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException();
                    }
                } else {
                    sql = "update " + this.tableName + " set " + this.forUpdateSql(value.get(0)) + " where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case SelectPage:
                Long begin = (pageQO.getCurrent() - 1L) * pageQO.getPageSize();
                sql = "select * from " + this.tableName;
                String append = " where ";
                if (isExtends) {
                    sql += " where deleted = false";
                    append = " and ";
                }
                if (condition != null && value != null && value.size() > 0) {
                    sql = appendCondition(sql, value.get(0), condition, append);
                }
                if (orderEnumMap != null && orderEnumMap.entrySet().size() > 0) {
                    sql = orderBy(sql, orderEnumMap);
                }
                sql += " limit " + begin + "," + pageQO.getPageSize();
                break;
            case SelectById:
                sql = "select * from " + this.tableName + " where " + idName + " = " + id;
                sql = isDelete(sql);
                break;
            case Disable:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set disabled = true,version = version + 1 , updated_at = \'" + TimeUtil.Now() + "\' where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException();
                    }
                } else {
                    sql = "update " + this.tableName + " set disabled = true where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case Enable:
                if (Init.version) {
                    if (isExtends) {
                        sql = "update " + this.tableName + " set disabled = false,version = version + 1 , updated_at = \'" + TimeUtil.Now() + "\' where " + idName + " = " + id;
                        sql = updateVersion(sql, id, value.get(0));
                    } else {
                        throw new ExtendsException();
                    }
                } else {
                    sql = "update " + this.tableName + " set disabled = false where " + idName + " = " + id;
                }
                sql = isDelete(sql);
                break;
            case Count:
                sql = "select count(1) from " + this.tableName;
                append = " where ";
                if (isExtends) {
                    sql += " where deleted = false";
                    append = " and ";
                }
                if (condition != null && value != null && value.size() > 0) {
                    sql = appendCondition(sql, value.get(0), condition, append);
                }
                break;
            case BatchSave:
                fieldAndValue = this.generateField(value);
                sql = "insert into " + this.tableName + "(" + fieldAndValue[0] + ") " + fieldAndValue[1];
                break;
        }
        log.info("Simple-Data : " + sql);
        return sql;
    }
}
