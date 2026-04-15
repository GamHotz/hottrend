package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * RSS 文章实体
 * 与 Python 版 TrendRadar 数据库表结构保持一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("rss_items")
public class RssItem {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 所属 RSS 源 ID
     */
    private Long feedId;

    /**
     * 文章链接
     */
    private String url;

    /**
     * RSS 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 作者
     */
    private String author;

    /**
     * 首次抓取时间
     */
    @TableField("first_crawl_time")
    private LocalDateTime firstCrawlTime;

    /**
     * 最后抓取时间
     */
    @TableField("last_crawl_time")
    private LocalDateTime lastCrawlTime;

    /**
     * 抓取次数
     */
    @TableField("crawl_count")
    private Integer crawlCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ================== 以下字段数据库中不存在，但保留用于业务逻辑 ==================

    /**
     * 文章内容（非数据库字段）
     */
    @TableField(exist = false)
    private String content;

    /**
     * 是否已推送（非数据库字段，用于业务逻辑）
     */
    @TableField(exist = false)
    private Boolean isPushed;

    /**
     * 标签（非数据库字段，用于业务逻辑）
     */
    @TableField(exist = false)
    private String tags;

    /**
     * AI 分析得分（非数据库字段，用于业务逻辑）
     */
    @TableField(exist = false)
    private Double aiScore;

    /**
     * 是否符合用户兴趣（非数据库字段，用于业务逻辑）
     */
    @TableField(exist = false)
    private Boolean isInteresting;
}