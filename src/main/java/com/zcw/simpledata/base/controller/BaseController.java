package com.zcw.simpledata.base.controller;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zcw.simpledata.base.annotations.Id;
import com.zcw.simpledata.base.entity.BaseEntity;
import com.zcw.simpledata.base.entity.qo.PageQO;
import com.zcw.simpledata.base.entity.vo.PageVO;
import com.zcw.simpledata.base.enums.SqlEnum;
import com.zcw.simpledata.base.exceptions.ApiException;
import com.zcw.simpledata.base.exceptions.ExtendsException;
import com.zcw.simpledata.base.exceptions.NullException;
import com.zcw.simpledata.base.mapper.ClassMapper;
import com.zcw.simpledata.config.Init;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@RestController
@Log4j2
public class BaseController<T, D> {
    private Class entityClass;
    private Class dtoClass;
    private String tableName;
    private String idName;
    private String id;
    private ClassMapper<T, D> classMapper;
    private static final String UNDERLINE = "_";
    private boolean isExtends = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BaseController() {
    }

    @SneakyThrows
    public BaseController(Class entity, Class dto) {
        this.entityClass = entity;
        this.dtoClass = dto;
        this.tableName = this.humpToUnderline(this.entityClass.getSimpleName()).toLowerCase();
        this.classMapper = new ClassMapper(this.entityClass, this.dtoClass);
        this.isExtends = (T) entityClass.newInstance() instanceof BaseEntity;
        for (Field field : entity.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                this.id = field.getName();
                this.idName = humpToUnderline(this.id);
            }
        }
    }

    public Field[] concat(Field[] first, Field[] second) {
        Field[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private String humpToUnderline(String para) {
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;
        if (!para.contains(UNDERLINE)) {
            for (int i = 1; i < para.length(); ++i) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, UNDERLINE);
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
    private String updateVersion(String sql, Long id) {
        T entity = classMapper.voTOEntity(queryById(id).getBody());
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

    private String generateSql(SqlEnum sqlEnum, List<T> value, Long id, PageQO pageQO) {
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
                        sql = updateVersion(sql, id);
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
                        sql = updateVersion(sql, id);
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
                        sql = updateVersion(sql, id);
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
                        sql = updateVersion(sql, id);
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

    @PostMapping(value = "/save")
    public ResponseEntity save(@RequestBody D vo) {
        T entity = this.classMapper.voTOEntity(vo);
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = this.generateSql(SqlEnum.Insert, entityList, null, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    @DeleteMapping(value = "/delete/false/{id}")
    public ResponseEntity deleteFalse(@PathVariable Long id) {
        if (isExtends) {
            String sql = this.generateSql(SqlEnum.DeleteFalse, null, id, null);
            int result = this.jdbcTemplate.update(sql);
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @DeleteMapping(value = "/delete/true/{id}")
    public ResponseEntity deleteTrue(@PathVariable Long id) {
        String sql = this.generateSql(SqlEnum.DeleteTrue, null, id, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping(value = "/update")
    @SneakyThrows
    public ResponseEntity update(@RequestBody D vo) {
        T entity = this.classMapper.voTOEntity(vo);
        String idName = this.idName.substring(0, 1).toUpperCase() + this.idName.substring(1);
        Method method = this.entityClass.getMethod("get" + this.id, (Class[]) null);
        Long id = (Long) method.invoke(entity, (Object[]) null);
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = this.generateSql(SqlEnum.Update, entityList, id, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    @GetMapping(value = "/queryById/{id}")
    public ResponseEntity<D> queryById(@PathVariable Long id) {
        String sql = this.generateSql(SqlEnum.SelectById, null, id, null);
        List<T> entityList = this.jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper(this.entityClass));
        T entity = (T) this.entityClass.newInstance();
        if (null != entityList && entityList.size() > 0) {
            entity = entityList.get(0);
        }
        D vo = this.classMapper.entityTOVo(entity);
        return ResponseEntity.ok(vo);
    }

    @GetMapping(value = "/queryPage")
    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO) {
        if (null == pageQO) {
            pageQO = new PageQO();
        }
        String sql = this.generateSql(SqlEnum.SelectPage, null, null, pageQO);
        List<T> entityList = this.jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper(this.entityClass));
        List<D> dtoList = this.classMapper.entityTOVo(entityList);
        PageVO<D> pageVO = new PageVO();
        pageVO.setCurrent(pageQO.getCurrent());
        pageVO.setPageSize(pageQO.getPageSize());
        pageVO.setData(dtoList);
        pageVO.setQueryNum(pageQO.getQueryNum());
        if (pageQO.getQueryNum()) {
            sql = this.generateSql(SqlEnum.Count, null, null, null);
            Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
            pageVO.setNum(num);
        }
        return ResponseEntity.ok(pageVO);
    }

    @SneakyThrows
    @PatchMapping(value = "/disable/{id}")
    public ResponseEntity disable(@PathVariable Long id) {
        if (isExtends) {
            String sql = this.generateSql(SqlEnum.Disable, null, id, null);
            int result = this.jdbcTemplate.update(sql);
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @SneakyThrows
    @PatchMapping(value = "/enable/{id}")
    public ResponseEntity enable(@PathVariable Long id) {
        if (isExtends) {
            String sql = this.generateSql(SqlEnum.Enable, null, id, null);
            int result = this.jdbcTemplate.update(sql);
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @GetMapping(value = "/queryCount")
    public ResponseEntity<Long> queryCount() {
        String sql = this.generateSql(SqlEnum.Count, null, (Long) null, null);
        Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
        return ResponseEntity.ok(num);
    }

    @PostMapping(value = "/batchSave")
    public ResponseEntity batchSave(@RequestBody List<D> voList) {
        List<T> entityList = classMapper.voTOEntity(voList);
        String sql = this.generateSql(SqlEnum.BatchSave, entityList, null, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping(value = "/batchDelete/true")
    public ResponseEntity batchDeleteTrue(@RequestBody List<Long> idList) {
        for (Long id : idList) {
            deleteTrue(id);
        }
        return ResponseEntity.ok().build();
    }

    @SneakyThrows
    @DeleteMapping(value = "/batchDelete/false")
    public ResponseEntity batchDeleteFalse(@RequestBody List<Long> idList) {
        if (isExtends) {
            for (Long id : idList) {
                deleteFalse(id);
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @PutMapping(value = "/batchUpdate")
    public ResponseEntity batchUpdate(@RequestBody List<D> voList) {
        for (D vo : voList) {
            update(vo);
        }
        return ResponseEntity.ok().build();
    }
}
