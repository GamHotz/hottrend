package com.hottrend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hottrend.mapper.KeywordMapper;
import com.hottrend.model.Keyword;
import com.hottrend.service.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 关键词服务实现类
 */
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements IKeywordService {

    private final KeywordMapper keywordMapper;

    @Override
    public List<Keyword> listAllKeywords() {
        return keywordMapper.selectList(null);
    }

    @Override
    public List<Keyword> listEnabledKeywords() {
        return keywordMapper.selectList(
                new QueryWrapper<Keyword>().eq("enabled", true)
        );
    }

    @Override
    public Keyword getKeywordById(Long id) {
        return keywordMapper.selectById(id);
    }

    @Override
    public List<Keyword> listKeywordsByGroupName(String groupName) {
        return keywordMapper.findByGroupName(groupName);
    }

    @Override
    public boolean saveKeyword(Keyword keyword) {
        return keywordMapper.insert(keyword) > 0;
    }

    @Override
    public List<Keyword> saveBatchKeywords(List<Keyword> keywords) {
        for (Keyword keyword : keywords) {
            keywordMapper.insert(keyword);
        }
        return keywords;
    }

    @Override
    public boolean updateKeyword(Keyword keyword) {
        return keywordMapper.updateById(keyword) > 0;
    }

    @Override
    public boolean deleteKeyword(Long id) {
        return keywordMapper.deleteById(id) > 0;
    }
}