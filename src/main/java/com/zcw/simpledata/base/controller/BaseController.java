package com.zcw.simpledata.base.controller;


import java.util.*;

import com.zcw.simpledata.base.entity.qo.PageQO;
import com.zcw.simpledata.base.entity.vo.PageVO;
import com.zcw.simpledata.base.enums.QueryEnum;
import com.zcw.simpledata.base.service.BaseService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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
public class BaseController<T, D> {

    private BaseService<T,D> baseService;

    private BaseController() {
    }

    public BaseController(Class entity, Class vo) {
        baseService=new BaseService(entity, vo);
    }

    @PostMapping(value = "/save")
    public ResponseEntity save(@RequestBody D vo) {
        return baseService.save(vo);
    }

    @DeleteMapping(value = "/delete/false/{id}")
    public ResponseEntity deleteFalse(@PathVariable Long id) {
        return baseService.deleteFalse(id);
    }

    @DeleteMapping(value = "/delete/true/{id}")
    public ResponseEntity deleteTrue(@PathVariable Long id) {
        return baseService.deleteTrue(id);
    }

    @PutMapping(value = "/update")
    public ResponseEntity update(@RequestBody D vo) {
        return baseService.update(vo);
    }

    @GetMapping(value = "/queryById/{id}")
    public ResponseEntity<D> queryById(@PathVariable Long id) {
        return baseService.queryById(id);
    }

    @GetMapping(value = "/queryPage")
    public ResponseEntity<PageVO<D>> queryPage(PageQO pageQO, D qo, @RequestBody Map<String, QueryEnum> condition) {
        return baseService.queryPage(pageQO, qo, condition);
    }

    @PatchMapping(value = "/disable/{id}")
    public ResponseEntity disable(@PathVariable Long id) {
        return baseService.disable(id);
    }

    @PatchMapping(value = "/enable/{id}")
    public ResponseEntity enable(@PathVariable Long id) {
        return baseService.enable(id);
    }

    @GetMapping(value = "/queryCount")
    public ResponseEntity<Long> queryCount(D qo, @RequestBody Map<String, QueryEnum> condition) {
        return baseService.queryCount(qo, condition);
    }

    @PostMapping(value = "/batchSave")
    public ResponseEntity batchSave(@RequestBody List<D> voList) {
        return baseService.batchSave(voList);
    }

    @DeleteMapping(value = "/batchDelete/true")
    public ResponseEntity batchDeleteTrue(@RequestBody List<Long> idList) {
        return baseService.batchDeleteTrue(idList);
    }

    @DeleteMapping(value = "/batchDelete/false")
    public ResponseEntity batchDeleteFalse(@RequestBody List<Long> idList) {
        return baseService.batchDeleteFalse(idList);
    }

    @PutMapping(value = "/batchUpdate")
    public ResponseEntity batchUpdate(@RequestBody List<D> voList) {
        return baseService.batchUpdate(voList);
    }
}
