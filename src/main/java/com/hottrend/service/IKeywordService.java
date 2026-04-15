package com.hottrend.service;

import com.hottrend.model.Keyword;

import java.util.List;

/**
 * 关键词服务接口
 */
public interface IKeywordService {

    /**
     * 获取所有关键词
     */
    List<Keyword> listAllKeywords();

    /**
     * 获取启用的关键词
     */
    List<Keyword> listEnabledKeywords();

    /**
     * 根据ID获取关键词
     */
    Keyword getKeywordById(Long id);

    /**
     * 根据分组获取关键词
     */
    List<Keyword> listKeywordsByGroupName(String groupName);

    /**
     * 保存关键词
     */
    boolean saveKeyword(Keyword keyword);

    /**
     * 批量保存关键词
     */
    List<Keyword> saveBatchKeywords(List<Keyword> keywords);

    /**
     * 更新关键词
     */
    boolean updateKeyword(Keyword keyword);

    /**
     * 删除关键词
     */
    boolean deleteKeyword(Long id);
}