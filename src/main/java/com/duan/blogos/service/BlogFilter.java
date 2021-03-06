package com.duan.blogos.service;

import com.duan.blogos.manager.BlogSortRule;

/**
 * Created on 2017/12/19.
 *
 * @author DuanJiaNing
 */
public interface BlogFilter<T> {

    /**
     * 全限定检索
     *
     * @param categoryIds 限定在博主的哪些类别之下，不做限定时传null
     * @param labelIds    限定在博主的哪些标签之下，不做限定时传null
     * @param keyWord     关键字,不做限定时传null
     * @param bloggerId   博主id
     * @param offset      结果集起始位置
     * @param rows        行数
     * @param sortRule    排序规则，为null则不做约束
     * @return 查询结果
     */
    T listFilterAll(int[] categoryIds, int[] labelIds,
                    String keyWord, int bloggerId, int offset,
                    int rows, BlogSortRule sortRule);

    /**
     * 标签&类别检索
     *
     * @param labelIds    限定在博主的哪些标签之下
     * @param categoryIds 限定在博主的哪些类别之下
     * @param bloggerId   博主id
     * @param offset      结果集起始位置
     * @param rows        行数
     * @param sortRule    排序规则，为null则不做约束
     * @return 查询结果
     */
    T listFilterByLabelAndCategory(int[] categoryIds, int[] labelIds, int bloggerId, int offset,
                                   int rows, BlogSortRule sortRule);

}
