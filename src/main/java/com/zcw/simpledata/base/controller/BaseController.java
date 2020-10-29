package com.zcw.simpledata.base.controller;


import java.lang.reflect.Method;
import java.util.*;

import com.zcw.simpledata.base.entity.CacheData;
import com.zcw.simpledata.base.entity.qo.PageQO;
import com.zcw.simpledata.base.entity.vo.PageVO;
import com.zcw.simpledata.base.enums.SqlEnum;
import com.zcw.simpledata.base.exceptions.NullException;
import com.zcw.simpledata.base.utils.SqlUtil;
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

    private boolean isExtends;

    private boolean isCache;

    private SqlUtil<T, D> sqlUtil;

    private Map<String, CacheData<D>> cacheDataMap;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BaseController() {
    }

    @SneakyThrows
    public BaseController(Class entity, Class vo) {
        this.sqlUtil = new SqlUtil(entity, vo);
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

    @PostMapping(value = "/save")
    public ResponseEntity save(@RequestBody D vo) {
        T entity = sqlUtil.classMapper.voTOEntity(vo);
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = sqlUtil.generateSql(SqlEnum.Insert, entityList, null, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    @DeleteMapping(value = "/delete/false/{id}")
    public ResponseEntity deleteFalse(@PathVariable Long id) {
        if (isExtends) {
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException("查找不到此实体");
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.DeleteFalse, value, id, null);
            int result = this.jdbcTemplate.update(sql);
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @DeleteMapping(value = "/delete/true/{id}")
    public ResponseEntity deleteTrue(@PathVariable Long id) {
        String sql = sqlUtil.generateSql(SqlEnum.DeleteTrue, null, id, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping(value = "/update")
    @SneakyThrows
    public ResponseEntity update(@RequestBody D vo) {
        T entity = sqlUtil.classMapper.voTOEntity(vo);
        String idName = sqlUtil.idName.substring(0, 1).toUpperCase() + sqlUtil.idName.substring(1);
        Method method = sqlUtil.entityClass.getMethod("get" + sqlUtil.id, (Class[]) null);
        Long id = (Long) method.invoke(entity, (Object[]) null);
        List<T> entityList = new ArrayList();
        entityList.add(entity);
        String sql = sqlUtil.generateSql(SqlEnum.Update, entityList, id, null);
        int result = this.jdbcTemplate.update(sql);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @SneakyThrows
    @GetMapping(value = "/queryById/{id}")
    public ResponseEntity<D> queryById(@PathVariable Long id) {
        String sql = sqlUtil.generateSql(SqlEnum.SelectById, null, id, null);
        List<D> cacheList = getCache(sql);
        if (cacheList == null || cacheList.size() == 0) {
            List<T> entityList = this.jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper(sqlUtil.entityClass));
            T entity = (T) sqlUtil.entityClass.newInstance();
            if (null != entityList && entityList.size() > 0) {
                entity = entityList.get(0);
            }
            D vo = sqlUtil.classMapper.entityTOVo(entity);
            if (isCache) {
                CacheData<D> cacheData = new CacheData();
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

    @GetMapping(value = "/queryPage")
    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO) {
        if (null == pageQO) {
            pageQO = new PageQO();
        }
        String sql = sqlUtil.generateSql(SqlEnum.SelectPage, null, null, pageQO);
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
            sql = sqlUtil.generateSql(SqlEnum.Count, null, null, null);
            Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
            pageVO.setNum(num);
        }
        return ResponseEntity.ok(pageVO);
    }

    @SneakyThrows
    @PatchMapping(value = "/disable/{id}")
    public ResponseEntity disable(@PathVariable Long id) {
        if (isExtends) {
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException("查找不到此实体");
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.Disable, value, id, null);
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
            T entity = sqlUtil.classMapper.voTOEntity(queryById(id).getBody());
            if (entity == null) {
                throw new NullException("查找不到此实体");
            }
            List<T> value = new ArrayList();
            value.add(entity);
            String sql = sqlUtil.generateSql(SqlEnum.Enable, value, id, null);
            int result = this.jdbcTemplate.update(sql);
            return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok("实体未继承BaseEntity");
        }
    }

    @GetMapping(value = "/queryCount")
    public ResponseEntity<Long> queryCount() {
        String sql = sqlUtil.generateSql(SqlEnum.Count, null, (Long) null, null);
        Long num = this.jdbcTemplate.queryForObject(sql, Long.class);
        return ResponseEntity.ok(num);
    }

    @PostMapping(value = "/batchSave")
    public ResponseEntity batchSave(@RequestBody List<D> voList) {
        List<T> entityList = sqlUtil.classMapper.voTOEntity(voList);
        String sql = sqlUtil.generateSql(SqlEnum.BatchSave, entityList, null, null);
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
