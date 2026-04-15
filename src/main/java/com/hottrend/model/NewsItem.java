package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 热榜新闻实体
 * 与 Python 版 TrendRadar 数据库表结构保持一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("news_items")
public class NewsItem {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 来源平台 ID
     */
    private String platformId;

    /**
     * 热度/排名
     */
    private Integer hotRank;

    /**
     * 新闻链接
     */
    private String url;

    /**
     * 移动端链接
     */
    private String mobileUrl;

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

    /**
     * 来源平台名称（非数据库字段）
     */
    private String platformName;

    // ================== 以下字段数据库中不存在，但保留用于业务逻辑 ==================


    /**
     * 热度值（非数据库字段）
     */
    @TableField(exist = false)
    private String hotValue;

    /**
     * 摘要（非数据库字段）
     */
    @TableField(exist = false)
    private String summary;

    /**
     * 发布时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime publishTime;

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