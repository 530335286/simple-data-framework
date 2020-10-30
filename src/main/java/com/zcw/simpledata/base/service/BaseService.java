package com.zcw.simpledata.base.service;

import com.zcw.simpledata.base.annotations.Id;
import com.zcw.simpledata.base.entity.CacheData;
import com.zcw.simpledata.base.entity.qo.PageQO;
import com.zcw.simpledata.base.entity.vo.PageVO;
import com.zcw.simpledata.base.enums.QueryEnum;
import com.zcw.simpledata.base.enums.SqlEnum;
import com.zcw.simpledata.base.exceptions.derive.ExtendsException;
import com.zcw.simpledata.base.exceptions.derive.IdException;
import com.zcw.simpledata.base.exceptions.derive.NullException;
import com.zcw.simpledata.base.utils.SqlUtil;
import com.zcw.simpledata.config.Init;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public BaseService(Class entity,Class vo) {
        this.sqlUtil = new SqlUtil(entity, vo, this);
        this.isCache = (Init.cacheTime != null);
        this.cacheDataMap = new HashMap();
    }

    private void setCache(List<D> data, String sql) {
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
        String sql = sqlUtil.generateSql(SqlEnum.Insert, entityList, null, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
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
            String sql = sqlUtil.generateSql(SqlEnum.DeleteFalse, value, id, null, null);
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
        String sql = sqlUtil.generateSql(SqlEnum.DeleteTrue, null, id, null, null);
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
        String sql = sqlUtil.generateSql(SqlEnum.Update, entityList, id, null, null);
        int result = this.jdbcTemplate.update(sql);
        cacheDataMap.clear();
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    public ResponseEntity<D> queryById(Long id) {
        if (id == null || id <= 0) {
            throw new IdException();
        }
        String sql = sqlUtil.generateSql(SqlEnum.SelectById, null, id, null, null);
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
            D vo = sqlUtil.classMapper.entityTOVo(entity);
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

    @SneakyThrows
    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO, D qo, Map<String, QueryEnum> condition) {
        if (null == pageQO) {
            pageQO = new PageQO();
        }
        T entity = null;
        List<T> list = new ArrayList();
        if (null != qo) {
            entity = sqlUtil.classMapper.voTOEntity(qo);
            list.add(entity);
        }
        String sql = sqlUtil.generateSql(SqlEnum.SelectPage, list, null, pageQO, condition);
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
            sql = sqlUtil.generateSql(SqlEnum.Count, list, null, null, condition);
            Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
            pageVO.setNum(num);
        }
        return ResponseEntity.ok(pageVO);
    }

    @SneakyThrows
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
            String sql = sqlUtil.generateSql(SqlEnum.Disable, value, id, null, null);
            int result = this.jdbcTemplate.update(sql);
            cacheDataMap.clear();
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            throw new ExtendsException();
        }
    }

    @SneakyThrows
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
            String sql = sqlUtil.generateSql(SqlEnum.Enable, value, id, null, null);
            int result = this.jdbcTemplate.update(sql);
            cacheDataMap.clear();
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            throw new ExtendsException();
        }
    }

    public ResponseEntity<Long> queryCount() {
        String sql = sqlUtil.generateSql(SqlEnum.Count, null, null, null, null);
        Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
        return ResponseEntity.ok(num);
    }

    public ResponseEntity batchSave(List<D> voList) {
        List<T> entityList = sqlUtil.classMapper.voTOEntity(voList);
        String sql = sqlUtil.generateSql(SqlEnum.BatchSave, entityList, null, null, null);
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

    @SneakyThrows
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
