package com.simpledata.frame.base.controller;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import com.simpledata.frame.base.annotations.Log;
import com.simpledata.frame.base.entity.qo.PageQO;
import com.simpledata.frame.base.entity.vo.PageVO;
import com.simpledata.frame.base.enums.QueryEnum;
import com.simpledata.frame.base.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@RestController
public abstract class BaseController<T, D> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BaseService<T, D> baseService;

    private Class<T> entity;
    private Class<D> vo;

    protected BaseController() {
        Type type = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] t = parameterizedType.getActualTypeArguments();
        entity = (Class<T>) t[0];
        vo = (Class<D>) t[1];
    }

    @PostConstruct
    void init() {
        baseService = new BaseService(jdbcTemplate, entity, vo);
    }

    @PostMapping(value = "/save")
    @Log(value = "/save")
    @Operation(summary = "新增")
    public ResponseEntity save(@RequestBody D vo) {
        return baseService.save(vo);
    }

    @DeleteMapping(value = "/delete/false/{id}")
    @Log(value = "/delete/false/{id}")
    @Operation(summary = "软删除")
    public ResponseEntity deleteFalse(@PathVariable Long id) {
        return baseService.deleteFalse(id);
    }

    @DeleteMapping(value = "/delete/true/{id}")
    @Log(value = "/delete/true/{id}")
    @Operation(summary = "物理删除")
    public ResponseEntity deleteTrue(@PathVariable Long id) {
        return baseService.deleteTrue(id);
    }

    @PutMapping(value = "/update")
    @Log(value = "/update")
    @Operation(summary = "更新")
    public ResponseEntity update(@RequestBody D vo) {
        return baseService.update(vo);
    }

    @GetMapping(value = "/getById/{id}")
    @Log(value = "/getById/{id}")
    @Operation(summary = "根据id查询")
    public ResponseEntity<D> queryById(@PathVariable Long id) {
        return baseService.queryById(id);
    }

    @GetMapping(value = "/getPage")
    @Log(value = "/getPage")
    @Operation(summary = "分页查询")
    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO, D vo, @RequestBody(required = false) List<Map<String, QueryEnum>> condition) {
        return baseService.queryPage(pageQO, vo, condition);
    }

    @PatchMapping(value = "/disable/{id}")
    @Log(value = "/disable/{id}")
    @Operation(summary = "禁用")
    public ResponseEntity disable(@PathVariable Long id) {
        return baseService.disable(id);
    }

    @PatchMapping(value = "/enable/{id}")
    @Log(value = "/enable/{id}")
    @Operation(summary = "启用")
    public ResponseEntity enable(@PathVariable Long id) {
        return baseService.enable(id);
    }

    @GetMapping(value = "/getCount")
    @Log(value = "/getCount")
    @Operation(summary = "查询数量")
    public ResponseEntity<Long> queryCount(D qo, @RequestBody(required = false) Map<String, QueryEnum> condition) {
        return baseService.queryCount(qo, condition);
    }

    @PostMapping(value = "/batchSave")
    @Log(value = "/batchSave")
    @Operation(summary = "批量新增")
    public ResponseEntity batchSave(@RequestBody List<D> voList) {
        return baseService.batchSave(voList);
    }

    @DeleteMapping(value = "/batchDelete/true")
    @Log(value = "/batchDelete/true")
    @Operation(summary = "批量物理删除")
    public ResponseEntity batchDeleteTrue(@RequestBody List<Long> idList) {
        return baseService.batchDeleteTrue(idList);
    }

    @DeleteMapping(value = "/batchDelete/false")
    @Log(value = "/batchDelete/false")
    @Operation(summary = "批量软删除")
    public ResponseEntity batchDeleteFalse(@RequestBody List<Long> idList) {
        return baseService.batchDeleteFalse(idList);
    }

    @PutMapping(value = "/batchUpdate")
    @Log(value = "/batchUpdate")
    @Operation(summary = "批量更新")
    public ResponseEntity batchUpdate(@RequestBody List<D> voList) {
        return baseService.batchUpdate(voList);
    }

    @DeleteMapping(value = "/clearCache")
    @Log(value = "/clearCache")
    @Operation(summary = "清除缓存")
    public ResponseEntity clearCache() {
        return baseService.clearCache();
    }
}
