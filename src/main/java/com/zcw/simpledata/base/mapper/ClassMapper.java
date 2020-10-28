package com.zcw.simpledata.base.mapper;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

public class ClassMapper<T, D> {
    private Class entityClass;
    private Class dtoClass;

    private ClassMapper() {
    }

    public ClassMapper(Class entity, Class dto) {
        this.entityClass = entity;
        this.dtoClass = dto;
    }

    public T voTOEntity(D vo) {
        T entity = this.getT();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    public List<T> voTOEntity(List<D> dtoList) {
        List<T> tList = new ArrayList();
        for (D vo : dtoList) {
            if (null != vo) {
                tList.add(this.voTOEntity(vo));
            }
        }
        return tList;
    }

    public D entityTOVo(T entity) {
        D vo = this.getD();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public List<D> entityTOVo(List<T> tList) {
        List<D> voList = new ArrayList();
        for (T entity : tList) {
            if (null != entity) {
                voList.add(this.entityTOVo(entity));
            }
        }
        return voList;
    }

    @SneakyThrows
    private T getT() {
        T entity = (T) this.entityClass.newInstance();
        return entity;
    }

    @SneakyThrows
    private D getD() {
        D dto = (D) this.dtoClass.newInstance();
        return dto;
    }
}
