package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.RssItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RSS 文章 Mapper
 */
@Mapper
public interface RssItemMapper extends BaseMapper<RssItem> {

    /**
     * 根据 URL 查找文章（用于去重）
     */
    @Select("SELECT * FROM rss_items WHERE url = #{url} LIMIT 1")
    RssItem findByUrl(@Param("url") String url);

    /**
     * 查找所有文章（按发布时间排序）
     */
    @Select("SELECT * FROM rss_items ORDER BY published_at DESC")
    List<RssItem> findByIsPushedFalseOrderByPublishTimeDesc();

    /**
     * 根据订阅源查找文章
     */
    @Select("SELECT * FROM rss_items WHERE feed_id = #{feedId} ORDER BY published_at DESC")
    List<RssItem> findBySourceId(@Param("feedId") String feedId);

    /**
     * 根据时间范围查找文章
     */
    @Select("SELECT * FROM rss_items WHERE published_at >= #{startTime} ORDER BY published_at DESC")
    List<RssItem> findByTimeRange(@Param("startTime") LocalDateTime startTime);

    /**
     * 查找所有文章
     */
    @Select("SELECT * FROM rss_items ORDER BY ai_score DESC")
    List<RssItem> findInterestingNews();
}