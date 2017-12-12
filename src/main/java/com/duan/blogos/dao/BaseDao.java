package com.duan.blogos.dao;

import java.util.List;

/**
 * Created on 2017/12/12.
 * Dao 层数据库操作的最上层约定
 *
 * @author DuanJiaNing
 */
public interface BaseDao<T> {

    /**
     * 根据id查询数据
     *
     * @param id
     * @return
     */
    List<T> query(int id);

    /**
     * 更新数据
     *
     * @param t
     * @return
     */
    int update(T t);

    /**
     * 根据id删除数据
     *
     * @param id
     * @return
     */
    int delete(int id);

    /**
     * 新增数据
     *
     * @param t
     * @return
     */
    int insert(T t);

}