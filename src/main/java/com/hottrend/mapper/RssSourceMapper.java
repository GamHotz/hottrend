package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.RssSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RSS 订阅源 Mapper
 */
@Mapper
public interface RssSourceMapper extends BaseMapper<RssSource> {

    /**
     * 根据分组查找订阅源
     */
    @Select("SELECT * FROM rss_feeds WHERE group_name = #{groupName} AND is_active = 1 ORDER BY name ASC")
    List<RssSource> findByGroupName(@Param("groupName") String groupName);

    /**
     * 查找所有启用的订阅源
     */
    @Select("SELECT * FROM rss_feeds WHERE is_active = 1")
    List<RssSource> findAllEnabled();

    /**
     * 根据 URL 查找订阅源
     */
    @Select("SELECT * FROM rss_feeds WHERE feed_url = #{url} LIMIT 1")
    RssSource findByUrl(@Param("url") String url);
}