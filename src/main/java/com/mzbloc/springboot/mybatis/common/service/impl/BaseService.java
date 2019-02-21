package com.mzbloc.springboot.mybatis.common.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mzbloc.springboot.mybatis.common.service.IBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by tanxw on 2017/3/30.
 */
public class BaseService<T> implements IBaseService<T> {
    @Autowired
    protected Mapper<T> mapper;

    public BaseService() {
    }

    public Mapper<T> getMapper() {
        return this.mapper;
    }

    public T selectByKey(Object key) {
        return this.mapper.selectByPrimaryKey(key);
    }

    public int save(T entity) {
        return this.mapper.insert(entity);
    }

    public int saveSelective(T entity) {
        return this.mapper.insertSelective(entity);
    }

    public int delete(Object key) {
        return this.mapper.deleteByPrimaryKey(key);
    }

    public int updateAll(T entity) {
        return this.mapper.updateByPrimaryKey(entity);
    }

    public int updateNotNull(T entity) {
        return this.mapper.updateByPrimaryKeySelective(entity);
    }

    public int updateByExample(T entity, Example example) {
        return this.mapper.updateByExampleSelective(entity, example);
    }

    public List<T> selectByList(Object example) {
        return this.mapper.selectByExample(example);
    }

    public PageInfo<T> selectByPage(Object example) {
        List resutl = this.mapper.selectByExample(example);
        PageInfo pageInfo = new PageInfo(resutl);
        return pageInfo;
    }

    public int countByExample(Example example) {
        return this.mapper.selectCountByExample(example);
    }

    public int deleteByExample(Example example) {
        return this.mapper.deleteByExample(example);
    }

    public T selectOne(Example example) {
        PageHelper.startPage(1, 1, false);
        List list = this.mapper.selectByExample(example);
        return list.isEmpty() ? null : (T) list.get(0);
    }
}
