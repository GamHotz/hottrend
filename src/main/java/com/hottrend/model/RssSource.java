package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * RSS 订阅源实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("rss_feeds")
public class RssSource {

    /**
     * 订阅源 ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订阅源名称
     */
    @TableField("name")
    private String name;

    /**
     * RSS/Atom URL (数据库字段)
     */
    @TableField("feed_url")
    private String feedUrl;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 最后抓取时间
     */
    @TableField("last_fetch_time")
    private LocalDateTime lastFetchTime;

    /**
     * 最后抓取状态
     */
    @TableField("last_fetch_status")
    private String lastFetchStatus;

    /**
     * 当日条目数
     */
    @TableField("item_count")
    private Integer itemCount;

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

    // ================== 以下字段数据库中不存在 ==================

    /**
     * 分组名称（非数据库字段）
     */
    @TableField(exist = false)
    private String groupName;

    /**
     * 最大保留天数（非数据库字段）
     */
    @TableField(exist = false)
    private Integer maxAgeDays;
}