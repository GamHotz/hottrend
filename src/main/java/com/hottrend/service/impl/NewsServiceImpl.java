package com.hottrend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hottrend.mapper.NewsMapper;
import com.hottrend.model.NewsItem;
import com.hottrend.service.INewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新闻服务实现类
 */
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements INewsService {

    private final NewsMapper newsMapper;

    @Override
    public IPage<NewsItem> listNews(String platformId, String keyword, int page, int pageSize) {
        Page<NewsItem> pageParam = new Page<>(page, pageSize);
        QueryWrapper<NewsItem> queryWrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(w -> w.like("title", keyword));
        }
        if (platformId != null && !platformId.isEmpty()) {
            queryWrapper.eq("platform_id", platformId);
        }

        queryWrapper.orderByDesc("first_crawl_time");

        return newsMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public NewsItem getNewsById(Long id) {
        return newsMapper.selectById(id);
    }

    @Override
    public boolean saveNews(NewsItem newsItem) {
        return newsMapper.insert(newsItem) > 0;
    }

    @Override
    public List<NewsItem> saveBatchNews(List<NewsItem> newsItems) {
        for (NewsItem item : newsItems) {
            newsMapper.insert(item);
        }
        return newsItems;
    }

    @Override
    public boolean updateNews(NewsItem newsItem) {
        return newsMapper.updateById(newsItem) > 0;
    }

    @Override
    public NewsItem getNewsByUrl(String url) {
        return newsMapper.findByUrl(url);
    }

    @Override
    public List<NewsItem> getUnpushedNews() {
        return newsMapper.findByIsPushedFalseOrderByRankAsc();
    }

    @Override
    public List<NewsItem> getNewsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return newsMapper.findByTimeRange(startTime, endTime);
    }

    @Override
    public List<NewsItem> getNewsByPlatformAndTimeRange(String platformId, LocalDateTime startTime) {
        return newsMapper.findByPlatformAndTimeRange(platformId, startTime);
    }

    @Override
    public Long countTodayNews(LocalDateTime date) {
        return newsMapper.countTodayNews(date);
    }

    @Override
    public Map<String, Long> countByPlatform() {
        Map<String, Long> result = new HashMap<>();
        newsMapper.countByPlatform().forEach(row ->
                result.put((String) row[0], (Long) row[1])
        );
        return result;
    }

    @Override
    public Long getTotalCount() {
        return newsMapper.selectCount(null);
    }

    @Override
    public void cleanupOldData(LocalDateTime cutoff) {
        QueryWrapper<NewsItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("first_crawl_time", cutoff);
        newsMapper.delete(queryWrapper);
    }
}