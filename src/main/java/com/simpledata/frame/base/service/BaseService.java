package com.simpledata.frame.base.service;

import com.simpledata.frame.base.annotations.Id;
import com.simpledata.frame.base.entity.CacheData;
import com.simpledata.frame.base.entity.qo.PageQO;
import com.simpledata.frame.base.entity.vo.PageVO;
import com.simpledata.frame.base.enums.OrderEnum;
import com.simpledata.frame.base.enums.QueryEnum;
import com.simpledata.frame.base.enums.SqlEnum;
import com.simpledata.frame.base.exceptions.derive.ExtendsException;
import com.simpledata.frame.base.exceptions.derive.IdException;
import com.simpledata.frame.base.exceptions.derive.NullException;
import com.simpledata.frame.base.utils.SqlUtil;
import com.simpledata.frame.config.Init;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class BaseService<T, D> {

    private boolean isCache;

    private SqlUtil<T, D> sqlUtil;

    private Map<String, CacheData<D>> cacheDataMap;

    public JdbcTemplate jdbcTemplate;

    public BaseService(JdbcTemplate jdbcTemplate,Class entity,Class vo) {
        Field[] fields = entity.getDeclaredFields();
        this.jdbcTemplate = jdbcTemplate;
        this.sqlUtil = new SqlUtil(entity, vo, this,fields);
        this.isCache = (Init.cacheTime != null);
        this.cacheDataMap = new HashMap();
    }

    private void setCache(List<D> data, String sql) {
        if (!isCache) {
            return;
        }
        Long time = System.currentTimeMillis() + (Init.cacheTime * 1000);
        CacheData<D> cacheData = new CacheData();
        cacheData.setTime(time);
        cacheData.setData(data);
        cacheDataMap.put(sql, cacheData);
    }

    private List<D> getCache(String sql) {
        CacheData<D> cacheData = cacheDataMap.get(sql);
        if (cacheData == null) {
            return null;
        }
        if (System.currentTimeMillis() > cacheData.getTime()) {
            cacheDataMap.remove(sql);
            return null;
        }
        List<D> data = cacheData.getData();
        return data;
    }

    public ResponseEntity save(D vo) {
        T entity = sqlUtil.classMapper.voTOEntity(vo);
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = sqlUtil.generateSql(SqlEnum.Insert, entityList, null, null, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    public ResponseEntity deleteFalse(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        if (sqlUtil.isExtends) {
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException();
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.DeleteFalse, value, id, null, null, null);
            int result = this.jdbcTemplate.update(sql);
            cacheDataMap.clear();
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            throw new ExtendsException();
        }
    }

    public ResponseEntity deleteTrue(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        String sql = sqlUtil.generateSql(SqlEnum.DeleteTrue, null, id, null, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    public ResponseEntity update(D vo) {
        T entity = sqlUtil.classMapper.voTOEntity(vo);
        String idName = sqlUtil.id.substring(0, 1).toUpperCase() + sqlUtil.id.substring(1);
        Method method = sqlUtil.entityClass.getMethod("get" + idName, null);
        Long id = (Long) method.invoke(entity, null);
        if (id == null || id == 0) {
            throw new IdException();

        }
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = sqlUtil.generateSql(SqlEnum.Update, entityList, id, null, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    public ResponseEntity<D> queryById(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        String sql = sqlUtil.generateSql(SqlEnum.SelectById, null, id, null, null, null);
        List<D> cacheList = getCache(sql);
        if (cacheList == null || cacheList.size() == 0) {
            List<T> entityList = this.jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper(sqlUtil.entityClass));
            T entity = null;
            if (null != entityList && entityList.size() > 0) {
                entity = entityList.get(0);
                Field[] fields = sqlUtil.entityClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Id.class)) {
                        String idName = field.getName();
                        idName = SqlUtil.getConvert(idName);
                        Method method = sqlUtil.entityClass.getMethod("get" + idName);
                        Long entityId = (Long) method.invoke(entity, null);
                        if (entityId == null || entityId == 0) {
                            ResponseEntity.ok(null);
                        }
                    }
                }

            } else {
                ResponseEntity.ok(null);
            }
            D vo = null;
            if (entity != null) {
                vo = sqlUtil.classMapper.entityTOVo(entity);
            }
            if (isCache) {
                List<D> data = new ArrayList();
                data.add(vo);
                setCache(data, sql);
            }
            return ResponseEntity.ok(vo);
        } else {
            setCache(cacheList, sql);
            return ResponseEntity.ok(cacheList.get(0));
        }

    }

    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO, D qo, Map<String, QueryEnum> condition, Map<String, OrderEnum> orderEnumMap) {
        if (null == pageQO) {
            pageQO = new PageQO();
        }
        T entity = null;
        List<T> list = new ArrayList();
        if (null != qo) {
            entity = sqlUtil.classMapper.voTOEntity(qo);
            list.add(entity);
        }
        String sql = sqlUtil.generateSql(SqlEnum.SelectPage, list, null, pageQO, condition, orderEnumMap);
        List<D> cacheList = getCache(sql);
        List<D> voList = null;
        if (cacheList == null || cacheList.size() == 0) {
            List<T> entityList = this.jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper(sqlUtil.entityClass));
            voList = sqlUtil.classMapper.entityTOVo(entityList);
        } else {
            voList = getCache(sql);
        }
        PageVO<D> pageVO = new PageVO();
        pageVO.setCurrent(pageQO.getCurrent());
        pageVO.setPageSize(pageQO.getPageSize());
        pageVO.setData(voList);
        pageVO.setQueryNum(pageQO.getQueryNum());
        setCache(voList, sql);
        if (pageQO.getQueryNum()) {
            sql = sqlUtil.generateSql(SqlEnum.Count, list, null, null, condition, null);
            Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
            pageVO.setTotal(num);
        }
        return ResponseEntity.ok(pageVO);
    }

    public ResponseEntity disable(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        if (sqlUtil.isExtends) {
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException();
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.Disable, value, id, null, null, null);
            int result = this.jdbcTemplate.update(sql);
            cacheDataMap.clear();
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            throw new ExtendsException();
        }
    }

    public ResponseEntity enable(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        if (sqlUtil.isExtends) {
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException();
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.Enable, value, id, null, null, null);
            int result = this.jdbcTemplate.update(sql);
            cacheDataMap.clear();
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            throw new ExtendsException();
        }
    }

    public ResponseEntity<Long> queryCount(D qo, Map<String, QueryEnum> condition) {
        List<T> list = new ArrayList<>();
        if (qo != null) {
            T entity = sqlUtil.classMapper.voTOEntity(qo);
            list.add(entity);
        }
        String sql = sqlUtil.generateSql(SqlEnum.Count, list, null, null, condition, null);
        Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
        return ResponseEntity.ok(num);
    }

    public ResponseEntity batchSave(List<D> voList) {
        List<T> entityList = sqlUtil.classMapper.voTOEntity(voList);
        String sql = sqlUtil.generateSql(SqlEnum.BatchSave, entityList, null, null, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    public ResponseEntity batchDeleteTrue(List<Long> idList) {
        for (Long id : idList) {
            if (id == null || id <= 0) {
                throw new IdException();
            }
            deleteTrue(id);
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity batchDeleteFalse(List<Long> idList) {
        if (sqlUtil.isExtends) {
            for (Long id : idList) {
                if (id == null || id <= 0) {
                    throw new IdException();
                }
                deleteFalse(id);
            }
            return ResponseEntity.ok().build();
        } else {
            throw new ExtendsException();
        }
    }

    public ResponseEntity batchUpdate(List<D> voList) {
        for (D vo : voList) {
            update(vo);
        }
        return ResponseEntity.ok().build();
    }

}
