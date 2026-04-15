package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.NewsItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻数据 Mapper
 */
@Mapper
public interface NewsMapper extends BaseMapper<NewsItem> {

    /**
     * 根据 URL 查找新闻（用于去重）
     */
    @Select("SELECT * FROM news_items WHERE url = #{url} LIMIT 1")
    NewsItem findByUrl(@Param("url") String url);

    /**
     * 查找所有新闻（按排名排序）
     */
    @Select("SELECT * FROM news_items ORDER BY hot_rank ASC")
    List<NewsItem> findByIsPushedFalseOrderByRankAsc();

    /**
     * 根据时间范围查找新闻
     */
    @Select("SELECT * FROM news_items WHERE first_crawl_time >= #{startTime} AND first_crawl_time <= #{endTime} ORDER BY first_crawl_time DESC")
    List<NewsItem> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计今日新增新闻数
     */
    @Select("SELECT COUNT(*) FROM news_items WHERE first_crawl_time >= #{date}")
    Long countTodayNews(@Param("date") LocalDateTime date);

    /**
     * 统计各平台新闻数
     */
    @Select("SELECT platform_id, COUNT(*) FROM news_items GROUP BY platform_id")
    List<Object[]> countByPlatform();

    /**
     * 查找符合用户兴趣的新闻（根据 ai_score 排序）
     */
    @Select("SELECT * FROM news_items WHERE ai_score IS NOT NULL ORDER BY ai_score DESC")
    List<NewsItem> findInterestingNews();

    /**
     * 根据平台和时间范围查找
     */
    @Select("SELECT * FROM news_items WHERE platform_id = #{platformId} AND first_crawl_time >= #{startTime} ORDER BY hot_rank ASC")
    List<NewsItem> findByPlatformAndTimeRange(@Param("platformId") String platformId, @Param("startTime") LocalDateTime startTime);
}